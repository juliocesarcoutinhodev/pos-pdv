package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    public ListUsersResult execute(ListUsersCommand command) {
        var pageResult = userRepository.findAll(
                command.filter(),
                command.page(),
                command.size()
        );

        log.debug("List users | page={} | size={} | total={}", command.page(), command.size(), pageResult.totalElements());

        var summaries = pageResult.content().stream()
                .map(user -> new ListUsersResult.UserSummary(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getProvider().name(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.isActive()
                ))
                .toList();

        return new ListUsersResult(
                summaries,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
