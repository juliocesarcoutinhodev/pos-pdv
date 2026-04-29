package br.com.topone.backend.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.seed.admin")
public class AdminSeedProperties {

    private boolean enabled = true;
    private String email = "admin@email.com";
    private String name = "Admin";
    private String password;
}
