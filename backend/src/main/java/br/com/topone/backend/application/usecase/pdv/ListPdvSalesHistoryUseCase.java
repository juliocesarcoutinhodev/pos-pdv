package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PdvSaleHistoryFilter;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListPdvSalesHistoryUseCase {

    private final PdvSaleRepository pdvSaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResult<PdvSaleHistoryResult> execute(ListPdvSalesHistoryCommand command) {
        var zone = ZoneId.systemDefault();
        var fromInclusive = command.dateFrom() != null ? command.dateFrom().atStartOfDay(zone).toInstant() : null;
        var toExclusive = command.dateTo() != null ? command.dateTo().plusDays(1).atStartOfDay(zone).toInstant() : null;

        var safePage = Math.max(0, command.page());
        var safeSize = Math.max(1, Math.min(command.size(), 100));

        var page = pdvSaleRepository.findHistory(
                new PdvSaleHistoryFilter(
                        command.userId(),
                        command.paymentMethod(),
                        fromInclusive,
                        toExclusive
                ),
                safePage,
                safeSize,
                command.sort()
        );

        var content = page.content().stream()
                .map(sale -> new PdvSaleHistoryResult(
                        sale.getId(),
                        sale.getCashRegisterSessionId(),
                        sale.getUserId(),
                        resolveUserName(sale.getUserId()),
                        sale.getPaymentMethod(),
                        normalizeMoney(sale.getTotalAmount()),
                        normalizeMoney(sale.getPaidAmount()),
                        normalizeMoney(sale.getChangeAmount()),
                        sale.getItems() != null ? sale.getItems().size() : 0,
                        sale.getNotes(),
                        sale.getCreatedAt()
                ))
                .toList();

        return new PageResult<>(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private String resolveUserName(UUID userId) {
        if (userId == null) {
            return "Usuário não informado";
        }
        return userRepository.findById(userId)
                .map(user -> user.getName())
                .orElse("Usuário não encontrado");
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
