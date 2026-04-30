package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.model.CashRegisterSession;
import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOpenCashRegistersForMonitoringUseCaseTest {

    @Mock
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @Mock
    private CashMovementRepository cashMovementRepository;
    @Mock
    private PdvSaleRepository pdvSaleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListOpenCashRegistersForMonitoringUseCase useCase;

    @Test
    void shouldReturnCashRegisterCardsIncludingClosedSessions() {
        var sessionId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var openSession = CashRegisterSession.builder()
                .id(sessionId)
                .userId(userId)
                .openingAmount(new BigDecimal("100.00"))
                .openedAt(Instant.now())
                .status(CashRegisterSessionStatus.OPEN)
                .build();
        var closedSession = CashRegisterSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .openingAmount(new BigDecimal("100.00"))
                .openedAt(Instant.now().minusSeconds(3600))
                .closedAt(Instant.now())
                .closingAmount(new BigDecimal("160.00"))
                .status(CashRegisterSessionStatus.CLOSED)
                .build();
        var user = User.builder()
                .id(userId)
                .name("Caixa 01")
                .email("caixa@pdv.com")
                .provider(AuthProvider.LOCAL)
                .build();

        when(cashRegisterSessionRepository.findOpenedBetween(any(Instant.class), any(Instant.class))).thenReturn(List.of(openSession, closedSession));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.SUPPLY)).thenReturn(new BigDecimal("20.00"));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.WITHDRAWAL)).thenReturn(new BigDecimal("10.00"));
        when(pdvSaleRepository.sumCashNetBySession(sessionId)).thenReturn(new BigDecimal("55.40"));
        when(cashMovementRepository.sumAmountBySessionAndType(closedSession.getId(), CashMovementType.SUPPLY)).thenReturn(new BigDecimal("20.00"));
        when(cashMovementRepository.sumAmountBySessionAndType(closedSession.getId(), CashMovementType.WITHDRAWAL)).thenReturn(new BigDecimal("10.00"));
        when(pdvSaleRepository.sumCashNetBySession(closedSession.getId())).thenReturn(new BigDecimal("50.00"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = useCase.execute(LocalDate.now(), LocalDate.now());

        assertThat(result).hasSize(2);
        var first = result.getFirst();
        assertThat(first.userName()).isEqualTo("Caixa 01");
        assertThat(first.status()).isEqualTo(CashRegisterSessionStatus.OPEN);
        assertThat(first.cashBalance()).isEqualByComparingTo("165.40");
        assertThat(first.closingAmount()).isNull();
        assertThat(first.differenceAmount()).isNull();

        var second = result.get(1);
        assertThat(second.status()).isEqualTo(CashRegisterSessionStatus.CLOSED);
        assertThat(second.closingAmount()).isEqualByComparingTo("160.00");
        assertThat(second.differenceAmount()).isEqualByComparingTo("0.00");
    }
}
