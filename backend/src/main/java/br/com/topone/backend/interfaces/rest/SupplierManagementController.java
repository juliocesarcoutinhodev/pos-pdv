package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.supplier.*;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.SupplierFilter;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierManagementController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "taxId", "createdAt");

    private final CreateSupplierUseCase createSupplierUseCase;
    private final ListSuppliersUseCase listSuppliersUseCase;
    private final GetSupplierByIdUseCase getSupplierByIdUseCase;
    private final UpdateSupplierUseCase updateSupplierUseCase;
    private final UpdateSupplierPatchUseCase updateSupplierPatchUseCase;
    private final DeactivateSupplierUseCase deactivateSupplierUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<SupplierListResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var result = listSuppliersUseCase.execute(new ListSuppliersCommand(
                new SupplierFilter(name, taxId, email, active),
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_SORT_FIELDS)
        ));

        var content = result.content().stream()
                .map(supplier -> new SupplierListResponse(
                        supplier.id(),
                        supplier.name(),
                        supplier.taxId(),
                        supplier.email(),
                        supplier.phone(),
                        supplier.createdAt(),
                        supplier.updatedAt(),
                        supplier.active()
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

    @GetMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<SupplierDetailResponse> getById(@PathVariable UUID id) {
        var result = getSupplierByIdUseCase.execute(new GetSupplierByIdCommand(id));
        return ResponseEntity.ok(new SupplierDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                toContactResponses(result.contacts()),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        ));
    }

    @PostMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<SupplierDetailResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        var result = createSupplierUseCase.execute(dtoCommandMapper.toCreateSupplierCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(new SupplierDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                toContactResponses(result.contacts()),
                result.createdAt(),
                null,
                true
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<SupplierDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        var result = updateSupplierUseCase.execute(dtoCommandMapper.toUpdateSupplierCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<SupplierDetailResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierPatchRequest request
    ) {
        var result = updateSupplierPatchUseCase.execute(dtoCommandMapper.toPatchSupplierCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateSupplierUseCase.execute(new DeactivateSupplierCommand(id));
        return ResponseEntity.noContent().build();
    }

    private SupplierDetailResponse toDetailResponse(UpdateSupplierResult result) {
        return new SupplierDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                toContactResponses(result.contacts()),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        );
    }

    private AddressResponse toAddressResponse(SupplierAddressResult address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.id(),
                address.zipCode(),
                address.street(),
                address.number(),
                address.complement(),
                address.district(),
                address.city(),
                address.state()
        );
    }

    private List<ContactResponse> toContactResponses(List<SupplierContactResult> contacts) {
        if (contacts == null) {
            return List.of();
        }

        return contacts.stream()
                .map(contact -> new ContactResponse(
                        contact.id(),
                        contact.name(),
                        contact.email(),
                        contact.phone()
                ))
                .toList();
    }
}
