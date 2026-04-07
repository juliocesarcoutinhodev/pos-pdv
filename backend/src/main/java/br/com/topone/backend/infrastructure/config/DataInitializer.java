package br.com.topone.backend.infrastructure.config;

import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.RoleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final String ADMIN_EMAIL = "admin@email.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USER_ROLE_NAME = "USER";
    private static final String ADMIN_ROLE_NAME = "ADMIN";

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            var userRole = ensureRole(roleRepository, USER_ROLE_NAME, "Usuário padrão");
            var adminRole = ensureRole(roleRepository, ADMIN_ROLE_NAME, "Administrador do sistema");
            var adminRoles = Set.of(userRole, adminRole);

            var existing = userRepository.findByEmailIncludingDeleted(ADMIN_EMAIL);
            if (existing.isPresent()) {
                log.debug("Admin user already exists | email={}", ADMIN_EMAIL);
                return;
            }

            var user = User.createLocalUser(ADMIN_EMAIL, "Admin", passwordEncoder.encode(ADMIN_PASSWORD));
            user.assignRoles(adminRoles);
            userRepository.save(user);
            log.info("Admin user created | email={}", ADMIN_EMAIL);
        };
    }

    private Role ensureRole(RoleRepository roleRepository, String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    var saved = roleRepository.save(Role.create(name, description));
                    log.info("Role seeded | name={} | id={}", saved.getName(), saved.getId());
                    return saved;
                });
    }
}
