package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListOpenCashRegistersForMonitoringUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OpenCashRegisterMonitorResult> execute(LocalDate dateFrom, LocalDate dateTo) {
        var zone = ZoneId.systemDefault();
        var effectiveDateFrom = dateFrom != null ? dateFrom : LocalDate.now(zone);
        var effectiveDateTo = dateTo != null ? dateTo : effectiveDateFrom;
        var fromInclusive = effectiveDateFrom.atStartOfDay(zone).toInstant();
        var toExclusive = effectiveDateTo.plusDays(1).atStartOfDay(zone).toInstant();

        return cashRegisterSessionRepository.findOpenedBetween(fromInclusive, toExclusive).stream()
                .map(session -> {
                    var supplies = CashRegisterBalanceCalculator.normalize(
                            cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY)
                    );
                    var withdrawals = CashRegisterBalanceCalculator.normalize(
                            cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL)
                    );
                    var cashNetSales = CashRegisterBalanceCalculator.normalize(pdvSaleRepository.sumCashNetBySession(session.getId()));
                    var balance = CashRegisterBalanceCalculator.calculate(
                            session.getOpeningAmount(),
                            supplies,
                            withdrawals,
                            cashNetSales
                    );
                    var closingAmount = CashRegisterBalanceCalculator.normalize(session.getClosingAmount());
                    var differenceAmount = session.getClosingAmount() != null
                            ? CashRegisterBalanceCalculator.normalize(closingAmount.subtract(balance))
                            : null;

                    var userName = userRepository.findById(session.getUserId())
                            .map(User::getName)
                            .orElse("Usuário não encontrado");

                    return new OpenCashRegisterMonitorResult(
                            session.getId(),
                            session.getUserId(),
                            userName,
                            session.getStatus(),
                            session.getOpenedAt(),
                            session.getClosedAt(),
                            balance,
                            session.getClosingAmount() != null ? closingAmount : null,
                            differenceAmount
                    );
                })
                .toList();
    }
}
