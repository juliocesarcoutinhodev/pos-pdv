package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.domain.repository.PageSort;

import java.time.LocalDate;
import java.util.UUID;

public record ListPdvSalesHistoryCommand(
        UUID userId,
        PdvPaymentMethod paymentMethod,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size,
        PageSort sort
) {
}
