package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.exception.RoleNotFoundException;
import br.com.topone.backend.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetRoleByIdUseCase {

    private final RoleRepository roleRepository;

    public GetRoleByIdResult execute(GetRoleByIdCommand command) {
        var role = roleRepository.findById(command.id())
                .orElseThrow(RoleNotFoundException::new);

        return new GetRoleByIdResult(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
