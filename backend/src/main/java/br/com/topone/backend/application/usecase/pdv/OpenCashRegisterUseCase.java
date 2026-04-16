package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionAlreadyOpenException;
import br.com.topone.backend.domain.exception.InvalidCashOperationException;
import br.com.topone.backend.domain.model.CashRegisterSession;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpenCashRegisterUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;

    @Transactional
    public CashRegisterStatusResult execute(OpenCashRegisterCommand command) {
        var openingAmount = CashRegisterBalanceCalculator.normalize(command.openingAmount());
        if (openingAmount.signum() < 0) {
            throw new InvalidCashOperationException("Valor de abertura do caixa não pode ser negativo.");
        }

        var hasOpenSession = cashRegisterSessionRepository.findOpenByUserId(command.userId()).isPresent();
        if (hasOpenSession) {
            throw new CashRegisterSessionAlreadyOpenException();
        }

        var created = CashRegisterSession.open(command.userId(), openingAmount);
        var saved = cashRegisterSessionRepository.save(created);

        return new CashRegisterStatusResult(
                saved.getId(),
                saved.getOpeningAmount(),
                saved.getOpenedAt(),
                CashRegisterBalanceCalculator.normalize(null),
                CashRegisterBalanceCalculator.normalize(null),
                CashRegisterBalanceCalculator.normalize(null),
                openingAmount
        );
    }
}

