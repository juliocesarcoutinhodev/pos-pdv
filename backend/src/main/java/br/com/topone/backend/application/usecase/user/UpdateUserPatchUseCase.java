package br.com.topone.backend.application.usecase.user;

import br.com.topone.backend.domain.exception.EmailAlreadyExistsException;
import br.com.topone.backend.domain.exception.UserNotFoundException;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserPatchUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UpdateUserPatchResult execute(UpdateUserPatchCommand command) {
        var user = userRepository.findById(command.id())
                .orElseThrow(UserNotFoundException::new);

        if (command.email() != null && !command.email().equals(user.getEmail())) {
            var existing = userRepository.findByEmailExcludingId(command.email(), user.getId());
            if (existing.isPresent()) {
                throw new EmailAlreadyExistsException();
            }
            user.changeEmail(command.email());
        }

        if (command.name() != null) {
            user.changeName(command.name());
        }

        if (command.password() != null && !command.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(command.password()));
        }

        if (command.roleIds() != null) {
            user.assignRoles(userRepository.resolveRolesByIds(command.roleIds()));
        }

        if (command.active() != null) {
            if (command.active()) {
                user.reactivate();
            } else {
                user.deactivate();
            }
        }

        user.touch();
        var saved = userRepository.save(user);
        log.info("User patched | id={}", saved.getId());

        return new UpdateUserPatchResult(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getProvider().name(),
                saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.isActive()
        );
    }
}
