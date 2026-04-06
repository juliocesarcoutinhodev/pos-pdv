package br.com.topone.backend.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.refresh-cookie")
public class RefreshTokenCookieProperties {

    private boolean enabled = true;
    private String name = "refresh_token";
    private String domain = "";
    private String path = "/api/v1/auth";
    private boolean secure = true;
    private boolean httpOnly = true;
    private String sameSite = "None";
    private long maxAge = 604800; // 7 days
}
