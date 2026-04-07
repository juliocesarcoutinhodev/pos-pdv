package br.com.topone.backend.application.usecase.user;

import br.com.topone.backend.domain.exception.EmailAlreadyExistsException;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.RoleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAdminUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateAdminUserResult execute(CreateAdminUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException();
        }

        var hashedPassword = passwordEncoder.encode(command.password());
        var user = User.createLocalUser(command.email(), command.name(), hashedPassword);
        user.assignRoles(roleRepository.resolveByIds(command.roleIds()));
        var saved = userRepository.save(user);
        log.info("Admin user created | email={} | id={}", saved.getEmail(), saved.getId());

        return new CreateAdminUserResult(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getRoles().stream().map(br.com.topone.backend.domain.model.Role::getName)
                        .collect(java.util.stream.Collectors.toSet()),
                saved.getCreatedAt()
        );
    }
}
