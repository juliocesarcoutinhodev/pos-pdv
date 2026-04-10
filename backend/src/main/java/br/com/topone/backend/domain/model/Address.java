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
public class Address {

    private UUID id;
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;

    public static Address create(
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state
    ) {
        var address = new Address();
        address.zipCode = normalizeZipCode(zipCode);
        address.street = normalizeText(street);
        address.number = normalizeNullableText(number);
        address.complement = normalizeNullableText(complement);
        address.district = normalizeText(district);
        address.city = normalizeText(city);
        address.state = normalizeState(state);
        return address;
    }

    public void update(
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state
    ) {
        this.zipCode = normalizeZipCode(zipCode);
        this.street = normalizeText(street);
        this.number = normalizeNullableText(number);
        this.complement = normalizeNullableText(complement);
        this.district = normalizeText(district);
        this.city = normalizeText(city);
        this.state = normalizeState(state);
    }

    private static String normalizeZipCode(String zipCode) {
        if (zipCode == null) {
            return null;
        }
        return zipCode.replaceAll("\\D", "");
    }

    private static String normalizeState(String state) {
        if (state == null) {
            return null;
        }
        return state.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
