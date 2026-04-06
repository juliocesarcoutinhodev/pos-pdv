package br.com.topone.backend.infrastructure.config;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.model.enums.Role;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final String ADMIN_EMAIL = "admin@email.com";

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            var existing = userRepository.findByEmailIncludingDeleted(ADMIN_EMAIL);
            if (existing.isPresent()) {
                log.debug("Admin user already exists | email={}", ADMIN_EMAIL);
                return;
            }

            var user = User.createLocalUser(ADMIN_EMAIL, "Admin", passwordEncoder.encode("admin123"));
            user.assignRoles(EnumSet.of(Role.USER, Role.ADMIN));
            userRepository.save(user);
            log.info("Admin user initialized | email={}", ADMIN_EMAIL);
        };
    }
}
