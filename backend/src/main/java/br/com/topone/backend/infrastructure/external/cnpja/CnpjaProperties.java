package br.com.topone.backend.infrastructure.external.cnpja;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * External provider settings for CNPJA integration.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration.cnpja")
public class CnpjaProperties {

    private String url;
    private String token;
}

