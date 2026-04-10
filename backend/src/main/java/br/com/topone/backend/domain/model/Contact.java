package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    private UUID id;
    private String name;
    private String email;
    private String phone;

    public static Contact create(String name, String email, String phone) {
        var contact = new Contact();
        contact.name = normalizeName(name);
        contact.email = normalizeEmail(email);
        contact.phone = normalizePhone(phone);
        return contact;
    }

    public void update(String name, String email, String phone) {
        this.name = normalizeName(name);
        this.email = normalizeEmail(email);
        this.phone = normalizePhone(phone);
    }

    private static String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        var normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        var normalized = phone.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
