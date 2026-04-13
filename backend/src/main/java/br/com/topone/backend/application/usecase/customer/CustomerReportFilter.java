package br.com.topone.backend.application.usecase.customer;

import java.time.LocalDate;

public record CustomerReportFilter(
        String nameFilter,
        Boolean activeFilter,
        Integer birthMonth,
        LocalDate birthDateFrom,
        LocalDate birthDateTo
) {
}
