package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.UserNotFoundException;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdResult execute(GetUserByIdCommand command) {
        var user = userRepository.findById(command.id())
                .orElseThrow(UserNotFoundException::new);

        return new GetUserByIdResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProvider().name(),
                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isActive()
        );
    }
}
