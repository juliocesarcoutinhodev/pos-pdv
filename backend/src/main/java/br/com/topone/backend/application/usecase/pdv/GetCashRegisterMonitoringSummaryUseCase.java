package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCashRegisterMonitoringSummaryUseCase {

    private static final int RECENT_SALES_LIMIT = 20;

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CashRegisterMonitoringSummaryResult execute(UUID sessionId) {
        var session = cashRegisterSessionRepository.findOpenById(sessionId)
                .orElseThrow(CashRegisterSessionNotOpenException::new);

        var userName = userRepository.findById(session.getUserId())
                .map(user -> user.getName())
                .orElse("Usuário não encontrado");

        var supplies = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY);
        var withdrawals = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL);
        var salesAmount = pdvSaleRepository.sumTotalAmountBySession(session.getId());
        var cashNetSales = pdvSaleRepository.sumCashNetBySession(session.getId());
        var balance = CashRegisterBalanceCalculator.calculate(
                session.getOpeningAmount(),
                supplies,
                withdrawals,
                cashNetSales
        );

        var paymentSummary = pdvSaleRepository.listTotalsByPaymentMethod(session.getId()).stream()
                .map(item -> new CashRegisterPaymentSummaryResult(
                        item.paymentMethod(),
                        CashRegisterBalanceCalculator.normalize(item.totalAmount()),
                        item.salesCount()
                ))
                .toList();

        var recentSales = pdvSaleRepository.findRecentBySession(session.getId(), RECENT_SALES_LIMIT).stream()
                .map(sale -> new CashRegisterSaleSummaryResult(
                        sale.getId(),
                        sale.getPaymentMethod(),
                        CashRegisterBalanceCalculator.normalize(sale.getTotalAmount()),
                        CashRegisterBalanceCalculator.normalize(sale.getPaidAmount()),
                        CashRegisterBalanceCalculator.normalize(sale.getChangeAmount()),
                        sale.getCreatedAt(),
                        sale.getItems() != null ? sale.getItems().size() : 0
                ))
                .toList();

        return new CashRegisterMonitoringSummaryResult(
                session.getId(),
                session.getUserId(),
                userName,
                session.getStatus(),
                session.getOpenedAt(),
                CashRegisterBalanceCalculator.normalize(session.getOpeningAmount()),
                CashRegisterBalanceCalculator.normalize(supplies),
                CashRegisterBalanceCalculator.normalize(withdrawals),
                CashRegisterBalanceCalculator.normalize(salesAmount),
                balance,
                pdvSaleRepository.countBySession(session.getId()),
                paymentSummary,
                recentSales
        );
    }
}
