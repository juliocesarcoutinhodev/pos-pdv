package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.exception.InvalidCashOperationException;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.model.CashRegisterSession;
import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCashRegisterUseCaseTest {

    @Mock
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @Mock
    private CashMovementRepository cashMovementRepository;
    @Mock
    private PdvSaleRepository pdvSaleRepository;

    @InjectMocks
    private CloseCashRegisterUseCase useCase;

    @Test
    void shouldCloseCashRegisterAndReturnConsolidatedSummary() {
        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var session = CashRegisterSession.builder()
                .id(sessionId)
                .userId(userId)
                .openingAmount(new BigDecimal("200.00"))
                .openedAt(Instant.parse("2026-04-29T08:00:00Z"))
                .status(CashRegisterSessionStatus.OPEN)
                .build();

        when(cashRegisterSessionRepository.findOpenByUserId(userId)).thenReturn(Optional.of(session));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.SUPPLY)).thenReturn(new BigDecimal("50.00"));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.WITHDRAWAL)).thenReturn(new BigDecimal("20.00"));
        when(pdvSaleRepository.sumTotalAmountBySession(sessionId)).thenReturn(new BigDecimal("500.00"));
        when(pdvSaleRepository.sumCashNetBySession(sessionId)).thenReturn(new BigDecimal("300.00"));
        when(cashRegisterSessionRepository.save(any(CashRegisterSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new CloseCashRegisterCommand(userId, new BigDecimal("540.00")));

        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.expectedCashAmount()).isEqualByComparingTo("530.00");
        assertThat(result.closingAmount()).isEqualByComparingTo("540.00");
        assertThat(result.differenceAmount()).isEqualByComparingTo("10.00");
        assertThat(result.salesAmount()).isEqualByComparingTo("500.00");
        assertThat(result.closedAt()).isNotNull();
        assertThat(session.getStatus()).isEqualTo(CashRegisterSessionStatus.CLOSED);
        assertThat(session.getClosingAmount()).isEqualByComparingTo("540.00");
        verify(cashRegisterSessionRepository).save(session);
    }

    @Test
    void shouldFailWhenNoOpenCashRegisterExistsForOperator() {
        var userId = UUID.randomUUID();
        when(cashRegisterSessionRepository.findOpenByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CloseCashRegisterCommand(userId, new BigDecimal("100.00"))))
                .isInstanceOf(CashRegisterSessionNotOpenException.class);
    }

    @Test
    void shouldFailWhenClosingAmountIsNegative() {
        var userId = UUID.randomUUID();
        var session = CashRegisterSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .openingAmount(new BigDecimal("100.00"))
                .status(CashRegisterSessionStatus.OPEN)
                .build();
        when(cashRegisterSessionRepository.findOpenByUserId(userId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.execute(new CloseCashRegisterCommand(userId, new BigDecimal("-1.00"))))
                .isInstanceOf(InvalidCashOperationException.class)
                .hasMessageContaining("não pode ser negativo");
    }
}
