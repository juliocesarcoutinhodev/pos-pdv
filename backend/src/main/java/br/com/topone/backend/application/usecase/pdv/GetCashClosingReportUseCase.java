package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GetCashClosingReportUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final PdvSaleRepository pdvSaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CashClosingReportResult execute(LocalDate referenceDate) {
        var zone = ZoneId.systemDefault();
        var date = referenceDate != null ? referenceDate : LocalDate.now(zone);
        var fromInclusive = date.atStartOfDay(zone).toInstant();
        var toExclusive = date.plusDays(1).atStartOfDay(zone).toInstant();

        var sessions = cashRegisterSessionRepository.findOpenedBetween(fromInclusive, toExclusive).stream()
                .map(session -> {
                    var supplies = normalizeMoney(cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.SUPPLY));
                    var withdrawals = normalizeMoney(cashMovementRepository.sumAmountBySessionAndType(session.getId(), CashMovementType.WITHDRAWAL));
                    var sales = normalizeMoney(pdvSaleRepository.sumTotalAmountBySession(session.getId()));
                    var cashNetSales = normalizeMoney(pdvSaleRepository.sumCashNetBySession(session.getId()));
                    var balance = normalizeMoney(CashRegisterBalanceCalculator.calculate(
                            session.getOpeningAmount(),
                            supplies,
                            withdrawals,
                            cashNetSales
                    ));

                    var userName = userRepository.findById(session.getUserId())
                            .map(User::getName)
                            .orElse("Usuário não encontrado");

                    return new CashClosingReportResult.CashClosingSessionResult(
                            session.getId(),
                            session.getUserId(),
                            userName,
                            session.getStatus(),
                            session.getOpenedAt(),
                            session.getClosedAt(),
                            normalizeMoney(session.getOpeningAmount()),
                            supplies,
                            withdrawals,
                            sales,
                            balance,
                            pdvSaleRepository.countBySession(session.getId())
                    );
                })
                .toList();

        var totalOpeningAmount = sessions.stream()
                .map(CashClosingReportResult.CashClosingSessionResult::openingAmount)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var totalSuppliesAmount = sessions.stream()
                .map(CashClosingReportResult.CashClosingSessionResult::suppliesAmount)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var totalWithdrawalsAmount = sessions.stream()
                .map(CashClosingReportResult.CashClosingSessionResult::withdrawalsAmount)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var totalSalesAmount = sessions.stream()
                .map(CashClosingReportResult.CashClosingSessionResult::salesAmount)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var totalCashBalance = sessions.stream()
                .map(CashClosingReportResult.CashClosingSessionResult::cashBalance)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var openSessions = sessions.stream()
                .filter(item -> item.status() == CashRegisterSessionStatus.OPEN)
                .count();

        return new CashClosingReportResult(
                date,
                sessions.size(),
                openSessions,
                sessions.size() - openSessions,
                normalizeMoney(totalOpeningAmount),
                normalizeMoney(totalSuppliesAmount),
                normalizeMoney(totalWithdrawalsAmount),
                normalizeMoney(totalSalesAmount),
                normalizeMoney(totalCashBalance),
                sessions
        );
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
