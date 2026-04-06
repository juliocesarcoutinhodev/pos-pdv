package br.com.topone.backend.infrastructure.config;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.Role;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.EnumSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final String ADMIN_EMAIL = "admin@email.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            var existing = userRepository.findByEmailIncludingDeleted(ADMIN_EMAIL);
            if (existing.isPresent()) {
                var admin = existing.get();
                if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPasswordHash())
                        || !admin.isActive()
                        || !admin.getRoles().contains(Role.ADMIN)) {
                    admin.changePassword(passwordEncoder.encode(ADMIN_PASSWORD));
                    admin.assignRoles(EnumSet.of(Role.USER, Role.ADMIN));
                    if (!admin.isActive()) {
                        admin.reactivate();
                    }
                    admin.touch();
                    userRepository.save(admin);
                    log.info("Admin user repaired | email={}", ADMIN_EMAIL);
                    return;
                }
                log.debug("Admin user already exists and valid | email={}", ADMIN_EMAIL);
                return;
            }

            var user = User.createLocalUser(ADMIN_EMAIL, "Admin", passwordEncoder.encode(ADMIN_PASSWORD));
            user.assignRoles(EnumSet.of(Role.USER, Role.ADMIN));
            userRepository.save(user);
            log.info("Admin user created | email={}", ADMIN_EMAIL);
        };
    }
}
