package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListOpenCashRegistersForMonitoringUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OpenCashRegisterMonitorResult> execute() {
        return cashRegisterSessionRepository.findAllOpenSessions().stream()
                .map(session -> {
                    var supplies = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY);
                    var withdrawals = cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL);
                    var cashNetSales = pdvSaleRepository.sumCashNetBySession(session.getId());
                    var balance = CashRegisterBalanceCalculator.calculate(
                            session.getOpeningAmount(),
                            supplies,
                            withdrawals,
                            cashNetSales
                    );

                    var userName = userRepository.findById(session.getUserId())
                            .map(user -> user.getName())
                            .orElse("Usuário não encontrado");

                    return new OpenCashRegisterMonitorResult(
                            session.getId(),
                            session.getUserId(),
                            userName,
                            session.getStatus(),
                            session.getOpenedAt(),
                            balance
                    );
                })
                .toList();
    }
}
