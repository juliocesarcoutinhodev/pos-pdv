package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleInUseException;
import br.com.topone.backend.domain.exception.RoleNotFoundException;
import br.com.topone.backend.domain.exception.SystemRoleDeletionNotAllowedException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRoleUseCaseTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DeleteRoleUseCase useCase;

    @Test
    void shouldThrowWhenRoleNotFound() {
        var roleId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteRoleCommand(roleId)))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void shouldBlockSystemRoleDeletion() {
        var roleId = UUID.randomUUID();
        var role = Role.create("ADMIN", "Administrador");
        role.setId(roleId);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> useCase.execute(new DeleteRoleCommand(roleId)))
                .isInstanceOf(SystemRoleDeletionNotAllowedException.class);

        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void shouldBlockDeletionWhenRoleIsAssignedToUsers() {
        var roleId = UUID.randomUUID();
        var role = Role.create("MANAGER", "Gerente");
        role.setId(roleId);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.isAssignedToAnyUser(roleId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new DeleteRoleCommand(roleId)))
                .isInstanceOf(RoleInUseException.class);

        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteCustomRoleWhenNotAssigned() {
        var roleId = UUID.randomUUID();
        var role = Role.create("MANAGER", "Gerente");
        role.setId(roleId);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.isAssignedToAnyUser(roleId)).thenReturn(false);

        useCase.execute(new DeleteRoleCommand(roleId));

        verify(roleRepository).deleteById(roleId);
    }
}
