package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleAlreadyExistsException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRoleUseCase {

    private final RoleRepository roleRepository;

    @Transactional
    public CreateRoleResult execute(CreateRoleCommand command) {
        var role = Role.create(command.name(), command.description());

        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RoleAlreadyExistsException();
        }

        var saved = roleRepository.save(role);
        log.info("Role created | name={} | id={}", saved.getName(), saved.getId());

        return new CreateRoleResult(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
    }
}
