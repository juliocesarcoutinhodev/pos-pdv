package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.*;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCashRegisterMonitoringSummaryUseCaseTest {

    @Mock
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @Mock
    private CashMovementRepository cashMovementRepository;
    @Mock
    private PdvSaleRepository pdvSaleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCashRegisterMonitoringSummaryUseCase useCase;

    @Test
    void shouldBuildMonitoringSummaryForOpenCashRegister() {
        var sessionId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var session = CashRegisterSession.builder()
                .id(sessionId)
                .userId(userId)
                .openingAmount(new BigDecimal("150.00"))
                .openedAt(Instant.now())
                .closedAt(Instant.now().plusSeconds(3600))
                .closingAmount(new BigDecimal("205.00"))
                .status(CashRegisterSessionStatus.OPEN)
                .build();
        var user = User.builder()
                .id(userId)
                .name("Operador Caixa")
                .email("caixa@pdv.com")
                .provider(AuthProvider.LOCAL)
                .build();
        var sale = PdvSale.builder()
                .id(UUID.randomUUID())
                .paymentMethod(PdvPaymentMethod.PIX)
                .totalAmount(new BigDecimal("35.90"))
                .paidAmount(new BigDecimal("35.90"))
                .changeAmount(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .items(List.of(PdvSaleItem.builder().id(UUID.randomUUID()).build()))
                .build();

        when(cashRegisterSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.SUPPLY)).thenReturn(new BigDecimal("20.00"));
        when(cashMovementRepository.sumAmountBySessionAndType(sessionId, CashMovementType.WITHDRAWAL)).thenReturn(new BigDecimal("5.00"));
        when(pdvSaleRepository.sumTotalAmountBySession(sessionId)).thenReturn(new BigDecimal("90.30"));
        when(pdvSaleRepository.sumCashNetBySession(sessionId)).thenReturn(new BigDecimal("40.00"));
        when(pdvSaleRepository.countBySession(sessionId)).thenReturn(3L);
        when(pdvSaleRepository.listTotalsByPaymentMethod(sessionId)).thenReturn(List.of(
                new PdvSaleRepository.PaymentMethodTotal(PdvPaymentMethod.CASH, new BigDecimal("54.40"), 2),
                new PdvSaleRepository.PaymentMethodTotal(PdvPaymentMethod.PIX, new BigDecimal("35.90"), 1)
        ));
        when(pdvSaleRepository.findRecentBySession(sessionId, 20)).thenReturn(List.of(sale));

        var result = useCase.execute(sessionId);

        assertThat(result.userName()).isEqualTo("Operador Caixa");
        assertThat(result.cashBalance()).isEqualByComparingTo("205.00");
        assertThat(result.closingAmount()).isEqualByComparingTo("205.00");
        assertThat(result.differenceAmount()).isEqualByComparingTo("0.00");
        assertThat(result.salesCount()).isEqualTo(3);
        assertThat(result.paymentSummary()).hasSize(2);
        assertThat(result.recentSales()).hasSize(1);
        assertThat(result.recentSales().getFirst().itemsCount()).isEqualTo(1);
    }
}
