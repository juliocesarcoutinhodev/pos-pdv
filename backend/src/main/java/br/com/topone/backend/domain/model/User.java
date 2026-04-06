package br.com.topone.backend.domain.model;

import br.com.topone.backend.domain.model.enums.AuthProvider;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private UUID id;
    private String email;
    private String name;
    private String passwordHash;
    private AuthProvider provider;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String email, String name, AuthProvider provider) {
        this.email = email;
        this.name = name;
        this.provider = provider;
    }

    public boolean isLocalAuth() {
        return provider == AuthProvider.LOCAL;
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
