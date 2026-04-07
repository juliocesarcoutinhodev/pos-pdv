package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleAlreadyExistsException;
import br.com.topone.backend.domain.exception.RoleNotFoundException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateRoleUseCase {

    private final RoleRepository roleRepository;

    @Transactional
    public UpdateRoleResult execute(UpdateRoleCommand command) {
        var role = roleRepository.findById(command.id())
                .orElseThrow(RoleNotFoundException::new);
        var normalizedName = Role.create(command.name(), command.description()).getName();

        if (roleRepository.findByNameExcludingId(normalizedName, command.id()).isPresent()) {
            throw new RoleAlreadyExistsException();
        }

        role.changeName(command.name());
        role.changeDescription(command.description());
        role.touch();

        var saved = roleRepository.save(role);
        log.info("Role updated | id={} | name={}", saved.getId(), saved.getName());

        return new UpdateRoleResult(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
