package br.com.topone.backend.domain.model;

import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.model.enums.Role;
import lombok.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Builder.Default
    private Set<Role> roles = EnumSet.noneOf(Role.class);
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public User(String email, String name, AuthProvider provider) {
        this.email = email;
        this.name = name;
        this.provider = provider;
    }

    public static User createLocalUser(String email, String name, String passwordHash) {
        var user = new User();
        user.email = email;
        user.name = name;
        user.setPasswordHash(passwordHash);
        user.provider = AuthProvider.LOCAL;
        return user;
    }

    public boolean isLocalAuth() {
        return provider == AuthProvider.LOCAL;
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }

    public void deactivate() {
        this.deletedAt = Instant.now();
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changePassword(String encodedPasswordHash) {
        this.passwordHash = encodedPasswordHash;
    }

    public void assignRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : EnumSet.noneOf(Role.class);
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
