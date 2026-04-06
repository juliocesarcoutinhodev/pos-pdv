package br.com.topone.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins()));
        config.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods()));
        config.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders()));
        config.setExposedHeaders(Arrays.asList(corsProperties.getExposedHeaders()));
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
