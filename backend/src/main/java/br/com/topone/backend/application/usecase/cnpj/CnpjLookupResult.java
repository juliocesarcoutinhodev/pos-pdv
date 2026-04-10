package br.com.topone.backend.application.usecase.cnpj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Canonical CNPJ lookup payload returned by the application layer.
 */
public record CnpjLookupResult(
        OffsetDateTime updated,
        String taxId,
        String name,
        String alias,
        LocalDate founded,
        BigDecimal equity,
        boolean head,
        Nature nature,
        CompanySize size,
        LocalDate statusDate,
        CompanyStatus status,
        Address address,
        List<Phone> phones,
        List<Email> emails,
        Activity mainActivity,
        List<Activity> sideActivities,
        List<Member> members
) {
    public record Nature(Integer id, String text) { }

    public record CompanySize(Integer id, String acronym, String text) { }

    public record CompanyStatus(Integer id, String text) { }

    public record Address(
            Integer municipality,
            String street,
            String number,
            String details,
            String district,
            String city,
            String state,
            String zip,
            Country country
    ) {
        public record Country(Integer id, String name) { }
    }

    public record Phone(String type, String area, String number) { }

    public record Email(String ownership, String address, String domain) { }

    public record Activity(Integer id, String text) { }

    public record Member(LocalDate since, Person person, MemberRole role) {
        public record Person(String name, String type, String taxId, String age) { }

        public record MemberRole(Integer id, String text) { }
    }
}

