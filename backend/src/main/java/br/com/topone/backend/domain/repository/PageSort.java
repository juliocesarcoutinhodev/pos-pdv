package br.com.topone.backend.domain.repository;

import java.util.Set;

public record PageSort(
        String field,
        SortDirection direction
) {

    public static PageSort by(String field, String direction, Set<String> allowedFields) {
        if (field == null || field.isBlank()) {
            return unsorted();
        }

        var normalizedField = field.trim();
        if (!allowedFields.contains(normalizedField)) {
            return unsorted();
        }

        return new PageSort(normalizedField, SortDirection.fromNullable(direction));
    }

    public static PageSort unsorted() {
        return new PageSort(null, SortDirection.ASC);
    }

    public boolean isSorted() {
        return field != null && !field.isBlank();
    }
}
