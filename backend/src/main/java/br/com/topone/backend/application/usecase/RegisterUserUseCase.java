package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.EmailAlreadyExistsException;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.repository.RoleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException();
        }

        var hashedPassword = passwordEncoder.encode(command.password());
        var user = User.createLocalUser(command.email(), command.name(), hashedPassword);
        user.assignRoles(roleRepository.resolveByNames(Set.of("USER")));
        var saved = userRepository.save(user);
        log.info("User registered successfully | email={} | id={}", saved.getEmail(), saved.getId());

        return new RegisterUserResult(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getProvider().name()
        );
    }
}
