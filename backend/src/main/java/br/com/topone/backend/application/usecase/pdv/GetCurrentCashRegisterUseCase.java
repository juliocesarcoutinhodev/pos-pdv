package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentCashRegisterUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;

    @Transactional(readOnly = true)
    public CashRegisterStatusResult execute(UUID userId) {
        var session = cashRegisterSessionRepository.findOpenByUserId(userId)
                .orElseThrow(CashRegisterSessionNotOpenException::new);

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

        return new CashRegisterStatusResult(
                session.getId(),
                session.getOpeningAmount(),
                session.getOpenedAt(),
                CashRegisterBalanceCalculator.normalize(supplies),
                CashRegisterBalanceCalculator.normalize(withdrawals),
                CashRegisterBalanceCalculator.normalize(salesAmount),
                balance
        );
    }
}

