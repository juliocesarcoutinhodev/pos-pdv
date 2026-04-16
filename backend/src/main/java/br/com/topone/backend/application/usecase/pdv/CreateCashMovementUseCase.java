package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.exception.InvalidCashOperationException;
import br.com.topone.backend.domain.model.CashMovement;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCashMovementUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;

    @Transactional
    public CashMovementResult execute(CreateCashMovementCommand command) {
        var session = cashRegisterSessionRepository.findOpenByUserId(command.userId())
                .orElseThrow(CashRegisterSessionNotOpenException::new);

        var amount = CashRegisterBalanceCalculator.normalize(command.amount());
        if (amount.signum() <= 0) {
            throw new InvalidCashOperationException("Valor da movimentação deve ser maior que zero.");
        }

        var supplies = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY);
        var withdrawals = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL);
        var cashNetSales = pdvSaleRepository.sumCashNetBySession(session.getId());
        var currentBalance = CashRegisterBalanceCalculator.calculate(
                session.getOpeningAmount(),
                supplies,
                withdrawals,
                cashNetSales
        );

        if (CashMovementType.WITHDRAWAL.equals(command.type()) && currentBalance.compareTo(amount) < 0) {
            throw new InvalidCashOperationException("Saldo insuficiente para realizar sangria.");
        }

        var movement = CashMovement.create(session.getId(), command.userId(), command.type(), amount, command.note());
        var saved = cashMovementRepository.save(movement);
        var nextBalance = CashMovementType.SUPPLY.equals(saved.getType())
                ? currentBalance.add(saved.getAmount())
                : currentBalance.subtract(saved.getAmount());

        return new CashMovementResult(
                saved.getId(),
                saved.getType(),
                saved.getAmount(),
                saved.getNote(),
                saved.getCreatedAt(),
                CashRegisterBalanceCalculator.normalize(nextBalance)
        );
    }
}

