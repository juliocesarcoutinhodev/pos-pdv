package br.com.topone.backend.domain.repository;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }

        return "DESC".equalsIgnoreCase(value) ? DESC : ASC;
    }
}
