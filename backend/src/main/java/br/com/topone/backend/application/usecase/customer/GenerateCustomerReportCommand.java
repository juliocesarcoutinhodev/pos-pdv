package br.com.topone.backend.application.usecase.customer;

import java.time.LocalDate;

public record GenerateCustomerReportCommand(
        CustomerReportType reportType,
        String nameFilter,
        Boolean activeFilter,
        Integer birthMonth,
        LocalDate birthDateFrom,
        LocalDate birthDateTo
) {

    public CustomerReportFilter toFilter() {
        return new CustomerReportFilter(nameFilter, activeFilter, birthMonth, birthDateFrom, birthDateTo);
    }
}
