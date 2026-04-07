package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleInUseException;
import br.com.topone.backend.domain.exception.RoleNotFoundException;
import br.com.topone.backend.domain.exception.SystemRoleDeletionNotAllowedException;
import br.com.topone.backend.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteRoleUseCase {

    private final RoleRepository roleRepository;

    @Transactional
    public void execute(DeleteRoleCommand command) {
        var role = roleRepository.findById(command.id())
                .orElseThrow(RoleNotFoundException::new);

        if (role.isSystemRole()) {
            throw new SystemRoleDeletionNotAllowedException();
        }

        if (roleRepository.isAssignedToAnyUser(command.id())) {
            throw new RoleInUseException();
        }

        roleRepository.deleteById(command.id());
        log.info("Role deleted | id={}", command.id());
    }
}
