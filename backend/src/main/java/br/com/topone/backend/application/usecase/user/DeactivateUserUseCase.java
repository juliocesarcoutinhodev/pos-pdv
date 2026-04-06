package br.com.topone.backend.application.usecase.user;

import br.com.topone.backend.domain.exception.UserNotFoundException;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public DeactivateUserResult execute(DeactivateUserCommand command) {
        var user = userRepository.findById(command.id())
                .orElseThrow(UserNotFoundException::new);

        user.deactivate();
        userRepository.save(user);
        log.info("User deactivated | id={}", command.id());

        return new DeactivateUserResult(command.id());
    }
}
