package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleAlreadyExistsException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRoleUseCaseTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CreateRoleUseCase useCase;

    @Test
    void shouldNormalizeAndCreateRole() {
        var command = new CreateRoleCommand("manager", "  Gerente da operação  ");
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            var role = invocation.getArgument(0, Role.class);
            role.setId(UUID.randomUUID());
            role.setCreatedAt(Instant.now());
            return role;
        });

        var result = useCase.execute(command);

        assertThat(result.name()).isEqualTo("MANAGER");
        assertThat(result.description()).isEqualTo("Gerente da operação");
    }

    @Test
    void shouldThrowWhenRoleAlreadyExists() {
        var command = new CreateRoleCommand("ADMIN", "Administrador");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(Role.create("ADMIN", "Administrador")));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RoleAlreadyExistsException.class);
    }
}
