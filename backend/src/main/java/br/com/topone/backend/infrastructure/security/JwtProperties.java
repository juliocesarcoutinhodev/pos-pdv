package br.com.topone.backend.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;
    private String issuer;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    private byte[] decodedSecret;

    @PostConstruct
    public void init() {
        this.decodedSecret = Base64.getDecoder().decode(secret);
    }
}
