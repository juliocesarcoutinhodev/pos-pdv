package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.role.CreateRoleUseCase;
import br.com.topone.backend.application.usecase.role.DeleteRoleCommand;
import br.com.topone.backend.application.usecase.role.DeleteRoleUseCase;
import br.com.topone.backend.application.usecase.role.GetRoleByIdCommand;
import br.com.topone.backend.application.usecase.role.GetRoleByIdUseCase;
import br.com.topone.backend.application.usecase.role.ListRolesCommand;
import br.com.topone.backend.application.usecase.role.ListRolesUseCase;
import br.com.topone.backend.application.usecase.role.UpdateRolePatchUseCase;
import br.com.topone.backend.application.usecase.role.UpdateRoleUseCase;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.CreateRoleRequest;
import br.com.topone.backend.interfaces.dto.DtoCommandMapper;
import br.com.topone.backend.interfaces.dto.PageResponse;
import br.com.topone.backend.interfaces.dto.RoleDetailResponse;
import br.com.topone.backend.interfaces.dto.RoleListResponse;
import br.com.topone.backend.interfaces.dto.UpdateRolePatchRequest;
import br.com.topone.backend.interfaces.dto.UpdateRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleManagementController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name");


    private final CreateRoleUseCase createRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleByIdUseCase getRoleByIdUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final UpdateRolePatchUseCase updateRolePatchUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<RoleListResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var result = listRolesUseCase.execute(new ListRolesCommand(
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_SORT_FIELDS)
        ));
        var content = result.content().stream()
                .map(role -> new RoleListResponse(
                        role.id(),
                        role.name(),
                        role.description(),
                        role.createdAt(),
                        role.updatedAt()
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
    public ResponseEntity<RoleDetailResponse> getById(@PathVariable UUID id) {
        var result = getRoleByIdUseCase.execute(new GetRoleByIdCommand(id));
        return ResponseEntity.ok(new RoleDetailResponse(
                result.id(),
                result.name(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        ));
    }

    @PostMapping
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<RoleDetailResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        var result = createRoleUseCase.execute(dtoCommandMapper.toCreateRoleCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(new RoleDetailResponse(
                result.id(),
                result.name(),
                result.description(),
                result.createdAt(),
                null
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<RoleDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        var result = updateRoleUseCase.execute(dtoCommandMapper.toUpdateRoleCommand(request, id));
        return ResponseEntity.ok(new RoleDetailResponse(
                result.id(),
                result.name(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<RoleDetailResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolePatchRequest request) {
        var result = updateRolePatchUseCase.execute(dtoCommandMapper.toPatchRoleCommand(request, id));
        return ResponseEntity.ok(new RoleDetailResponse(
                result.id(),
                result.name(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteRoleUseCase.execute(new DeleteRoleCommand(id));
        return ResponseEntity.noContent().build();
    }
}
