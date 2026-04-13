package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.label.CreateLabelPrintJobUseCase;
import br.com.topone.backend.application.usecase.label.CreateLabelPrintJobResult;
import br.com.topone.backend.application.usecase.label.GetLabelPrintJobByIdCommand;
import br.com.topone.backend.application.usecase.label.GetLabelPrintJobByIdUseCase;
import br.com.topone.backend.application.usecase.label.ListLabelPrintJobsCommand;
import br.com.topone.backend.application.usecase.label.ListLabelPrintJobsUseCase;
import br.com.topone.backend.application.usecase.label.ListLabelSuggestionsCommand;
import br.com.topone.backend.application.usecase.label.ListLabelSuggestionsUseCase;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.CreateLabelPrintJobRequest;
import br.com.topone.backend.interfaces.dto.DtoCommandMapper;
import br.com.topone.backend.interfaces.dto.LabelPrintJobItemResponse;
import br.com.topone.backend.interfaces.dto.LabelPrintJobResponse;
import br.com.topone.backend.interfaces.dto.LabelPrintJobSummaryResponse;
import br.com.topone.backend.interfaces.dto.LabelPrintSuggestionResponse;
import br.com.topone.backend.interfaces.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/labels")
@RequiredArgsConstructor
public class LabelPrintManagementController {

    private static final Set<String> ALLOWED_SUGGESTION_SORT_FIELDS = Set.of(
            "name",
            "sku",
            "category",
            "createdAt"
    );

    private static final Set<String> ALLOWED_HISTORY_SORT_FIELDS = Set.of(
            "createdAt",
            "referenceDate"
    );

    private final ListLabelSuggestionsUseCase listLabelSuggestionsUseCase;
    private final CreateLabelPrintJobUseCase createLabelPrintJobUseCase;
    private final ListLabelPrintJobsUseCase listLabelPrintJobsUseCase;
    private final GetLabelPrintJobByIdUseCase getLabelPrintJobByIdUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping("/suggestions")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<LabelPrintSuggestionResponse>> listSuggestions(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var result = listLabelSuggestionsUseCase.execute(new ListLabelSuggestionsCommand(
                date,
                name,
                sku,
                category,
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_SUGGESTION_SORT_FIELDS)
        ));

        var content = result.content().stream()
                .map(item -> new LabelPrintSuggestionResponse(
                        item.id(),
                        item.sku(),
                        item.barcode(),
                        item.name(),
                        item.category(),
                        item.unit(),
                        item.salePrice(),
                        item.promotionalPrice(),
                        item.createdAt()
                ))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @PostMapping("/jobs")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<LabelPrintJobResponse> createJob(@Valid @RequestBody CreateLabelPrintJobRequest request) {
        var result = createLabelPrintJobUseCase.execute(dtoCommandMapper.toCreateLabelPrintJobCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping("/jobs")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<LabelPrintJobSummaryResponse>> listJobs(
            @RequestParam(required = false) LocalDate referenceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var result = listLabelPrintJobsUseCase.execute(new ListLabelPrintJobsCommand(
                referenceDate,
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_HISTORY_SORT_FIELDS)
        ));

        var content = result.content().stream()
                .map(item -> new LabelPrintJobSummaryResponse(
                        item.id(),
                        item.referenceDate(),
                        item.totalProducts(),
                        item.totalLabels(),
                        item.createdAt()
                ))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<LabelPrintJobResponse> getJobById(@PathVariable UUID id) {
        var result = getLabelPrintJobByIdUseCase.execute(new GetLabelPrintJobByIdCommand(id));
        return ResponseEntity.ok(toResponse(result));
    }

    private LabelPrintJobResponse toResponse(CreateLabelPrintJobResult result) {
        var items = result.items().stream()
                .map(item -> new LabelPrintJobItemResponse(
                        item.productId(),
                        item.sku(),
                        item.barcode(),
                        item.name(),
                        item.unit(),
                        item.salePrice(),
                        item.promotionalPrice(),
                        item.quantity()
                ))
                .toList();

        return new LabelPrintJobResponse(
                result.id(),
                result.referenceDate(),
                result.totalProducts(),
                result.totalLabels(),
                result.createdAt(),
                items
        );
    }
}
