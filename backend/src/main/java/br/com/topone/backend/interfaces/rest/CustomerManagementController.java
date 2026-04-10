package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.customer.*;
import br.com.topone.backend.domain.repository.CustomerFilter;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerManagementController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "taxId", "createdAt");

    private final CreateCustomerUseCase createCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final UpdateCustomerPatchUseCase updateCustomerPatchUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<CustomerListResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var result = listCustomersUseCase.execute(new ListCustomersCommand(
                new CustomerFilter(name, taxId, email, active),
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_SORT_FIELDS)
        ));

        var content = result.content().stream()
                .map(customer -> new CustomerListResponse(
                        customer.id(),
                        customer.name(),
                        customer.taxId(),
                        customer.email(),
                        customer.phone(),
                        customer.imageId(),
                        customer.createdAt(),
                        customer.updatedAt(),
                        customer.active()
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
    public ResponseEntity<CustomerDetailResponse> getById(@PathVariable UUID id) {
        var result = getCustomerByIdUseCase.execute(new GetCustomerByIdCommand(id));
        return ResponseEntity.ok(new CustomerDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                result.imageId(),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        ));
    }

    @PostMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<CustomerDetailResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        var result = createCustomerUseCase.execute(dtoCommandMapper.toCreateCustomerCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(new CustomerDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                result.imageId(),
                result.createdAt(),
                null,
                true
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<CustomerDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        var result = updateCustomerUseCase.execute(dtoCommandMapper.toUpdateCustomerCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<CustomerDetailResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerPatchRequest request
    ) {
        var result = updateCustomerPatchUseCase.execute(dtoCommandMapper.toPatchCustomerCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateCustomerUseCase.execute(new DeactivateCustomerCommand(id));
        return ResponseEntity.noContent().build();
    }

    private CustomerDetailResponse toDetailResponse(UpdateCustomerResult result) {
        return new CustomerDetailResponse(
                result.id(),
                result.name(),
                result.taxId(),
                result.email(),
                result.phone(),
                toAddressResponse(result.address()),
                result.imageId(),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        );
    }

    private AddressResponse toAddressResponse(CustomerAddressResult address) {
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
}
