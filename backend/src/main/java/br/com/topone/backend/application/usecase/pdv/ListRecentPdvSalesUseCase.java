package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListRecentPdvSalesUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final PdvSaleRepository pdvSaleRepository;

    @Transactional(readOnly = true)
    public List<PdvSaleResult> execute(UUID userId, int limit) {
        var session = cashRegisterSessionRepository.findOpenByUserId(userId)
                .orElseThrow(CashRegisterSessionNotOpenException::new);

        var safeLimit = Math.max(1, Math.min(limit, 50));
        return pdvSaleRepository.findRecentBySession(session.getId(), safeLimit).stream()
                .map(sale -> new PdvSaleResult(
                        sale.getId(),
                        sale.getPaymentMethod(),
                        sale.getSubtotalAmount(),
                        sale.getDiscountAmount(),
                        sale.getTotalAmount(),
                        sale.getPaidAmount(),
                        sale.getChangeAmount(),
                        sale.getCreatedAt(),
                        sale.getItems().stream()
                                .map(item -> new PdvSaleItemResult(
                                        item.getProductId(),
                                        item.getSku(),
                                        item.getBarcode(),
                                        item.getName(),
                                        item.getUnit(),
                                        item.getUnitPrice(),
                                        item.getQuantity(),
                                        item.getLineTotal()
                                ))
                                .toList()
                ))
                .toList();
    }
}

