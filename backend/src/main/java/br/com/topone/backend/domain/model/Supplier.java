package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    private UUID id;
    private String name;
    private String taxId;
    private String email;
    private String phone;
    private Address address;
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public static Supplier create(
            String name,
            String taxId,
            String email,
            String phone,
            Address address,
            List<Contact> contacts
    ) {
        var supplier = new Supplier();
        supplier.name = normalizeName(name);
        supplier.taxId = normalizeTaxId(taxId);
        supplier.email = normalizeEmail(email);
        supplier.phone = normalizePhone(phone);
        supplier.address = address;
        supplier.contacts = normalizeContacts(contacts);
        return supplier;
    }

    public void changeName(String name) {
        this.name = normalizeName(name);
    }

    public void changeTaxId(String taxId) {
        this.taxId = normalizeTaxId(taxId);
    }

    public void changeEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public void changePhone(String phone) {
        this.phone = normalizePhone(phone);
    }

    public void changeAddress(Address address) {
        this.address = address;
    }

    public void assignContacts(List<Contact> contacts) {
        this.contacts = normalizeContacts(contacts);
    }

    public void deactivate() {
        this.deletedAt = Instant.now();
    }

    public void reactivate() {
        this.deletedAt = null;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public static String normalizeTaxId(String taxId) {
        if (taxId == null) {
            return null;
        }
        return taxId.replaceAll("\\D", "");
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

    private static List<Contact> normalizeContacts(List<Contact> contacts) {
        if (contacts == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(contacts);
    }
}
