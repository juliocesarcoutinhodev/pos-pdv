package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.user.*;
import br.com.topone.backend.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final CreateAdminUserUseCase createAdminUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateUserPatchUseCase updateUserPatchUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping
    public ResponseEntity<PageResponse<UserListResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active) {
        var command = new ListUsersCommand(
                new br.com.topone.backend.domain.repository.UserFilter(name, email, active),
                page, size
        );
        var result = listUsersUseCase.execute(command);

        var content = result.content().stream()
                .map(u -> new UserListResponse(
                        u.id(), u.email(), u.name(), u.provider(),
                        u.createdAt(), u.updatedAt(), u.active()))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                content, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getById(@PathVariable UUID id) {
        var result = getUserByIdUseCase.execute(new GetUserByIdCommand(id));
        return ResponseEntity.ok(new UserDetailResponse(
                result.id(), result.email(), result.name(), result.provider(),
                result.roles(), result.createdAt(), result.updatedAt(), result.active()));
    }

    @PostMapping
    public ResponseEntity<UserDetailResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var command = dtoCommandMapper.toCreateUserCommand(request);
        var result = createAdminUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDetailResponse(
                result.id(), result.email(), result.name(), "LOCAL",
                result.roles(), result.createdAt(), null, true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        var command = dtoCommandMapper.toUpdateUserCommand(request, id);
        var result = updateUserUseCase.execute(command);
        return ResponseEntity.ok(new UserDetailResponse(
                result.id(), result.email(), result.name(), result.provider(),
                result.roles(), result.createdAt(), result.updatedAt(), result.active()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDetailResponse> patch(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserPatchRequest request) {
        var command = dtoCommandMapper.toPatchUserCommand(request, id);
        var result = updateUserPatchUseCase.execute(command);
        return ResponseEntity.ok(new UserDetailResponse(
                result.id(), result.email(), result.name(), result.provider(),
                result.roles(), result.createdAt(), result.updatedAt(), result.active()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.execute(new DeactivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }
}
