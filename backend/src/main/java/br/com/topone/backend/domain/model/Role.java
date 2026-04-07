package br.com.topone.backend.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    private static final Set<String> SYSTEM_ROLE_NAMES = Set.of("USER", "ADMIN");

    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static Role create(String name, String description) {
        var role = new Role();
        role.name = normalizeName(name);
        role.description = normalizeDescription(description);
        return role;
    }

    public void changeName(String name) {
        this.name = normalizeName(name);
    }

    public void changeDescription(String description) {
        this.description = normalizeDescription(description);
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public boolean isSystemRole() {
        return name != null && SYSTEM_ROLE_NAMES.contains(name);
    }

    private static String normalizeName(String name) {
        return name == null ? null : name.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        var normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
