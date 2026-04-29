package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.exception.InvalidCashOperationException;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CloseCashRegisterUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;

    @Transactional
    public CloseCashRegisterResult execute(CloseCashRegisterCommand command) {
        var session = cashRegisterSessionRepository.findOpenByUserId(command.userId())
                .orElseThrow(CashRegisterSessionNotOpenException::new);

        var closingAmount = CashRegisterBalanceCalculator.normalize(command.closingAmount());
        if (closingAmount.signum() < 0) {
            throw new InvalidCashOperationException("Valor informado no fechamento não pode ser negativo.");
        }

        var supplies = CashRegisterBalanceCalculator.normalize(
                cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY)
        );
        var withdrawals = CashRegisterBalanceCalculator.normalize(
                cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL)
        );
        var salesAmount = CashRegisterBalanceCalculator.normalize(pdvSaleRepository.sumTotalAmountBySession(session.getId()));
        var cashNetSales = CashRegisterBalanceCalculator.normalize(pdvSaleRepository.sumCashNetBySession(session.getId()));
        var expectedCashAmount = CashRegisterBalanceCalculator.calculate(
                session.getOpeningAmount(),
                supplies,
                withdrawals,
                cashNetSales
        );
        var differenceAmount = CashRegisterBalanceCalculator.normalize(closingAmount.subtract(expectedCashAmount));

        session.setClosingAmount(closingAmount);
        session.setClosedAt(Instant.now());
        session.setStatus(CashRegisterSessionStatus.CLOSED);
        var saved = cashRegisterSessionRepository.save(session);

        return new CloseCashRegisterResult(
                saved.getId(),
                saved.getUserId(),
                CashRegisterBalanceCalculator.normalize(saved.getOpeningAmount()),
                supplies,
                withdrawals,
                salesAmount,
                expectedCashAmount,
                closingAmount,
                differenceAmount,
                saved.getOpenedAt(),
                saved.getClosedAt()
        );
    }
}
