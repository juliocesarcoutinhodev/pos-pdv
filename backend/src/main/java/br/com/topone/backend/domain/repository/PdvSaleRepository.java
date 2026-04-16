package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.PdvSale;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PdvSaleRepository {

    PdvSale save(PdvSale sale);

    BigDecimal sumTotalAmountBySession(UUID sessionId);

    BigDecimal sumCashNetBySession(UUID sessionId);

    List<PdvSale> findRecentBySession(UUID sessionId, int limit);
}

