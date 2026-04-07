package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListRolesUseCase {

    private final RoleRepository roleRepository;

    public ListRolesResult execute(ListRolesCommand command) {
        var pageResult = roleRepository.findAll(command.page(), command.size());

        log.debug("List roles | page={} | size={} | total={}", command.page(), command.size(), pageResult.totalElements());

        var summaries = pageResult.content().stream()
                .map(role -> new ListRolesResult.RoleSummary(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getCreatedAt(),
                        role.getUpdatedAt()
                ))
                .toList();

        return new ListRolesResult(
                summaries,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
