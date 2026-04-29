package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.time.Instant;
import java.util.UUID;

public record PdvSaleHistoryFilter(
        UUID userId,
        PdvPaymentMethod paymentMethod,
        Instant fromInclusive,
        Instant toExclusive
) {
}
