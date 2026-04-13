package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.InvalidCustomerReportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCustomerReportUseCaseTest {

    @Mock
    private CustomerReportGateway customerReportGateway;

    @InjectMocks
    private GenerateCustomerReportUseCase useCase;

    @Test
    void shouldGenerateSummaryReportWhenFiltersAreValid() {
        var command = new GenerateCustomerReportCommand(
                CustomerReportType.SUMMARY,
                "João",
                true,
                8,
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2000, 12, 31)
        );

        when(customerReportGateway.generateSummaryReportPdf(command.toFilter())).thenReturn(new byte[]{1, 2, 3});

        var result = useCase.execute(command);

        assertThat(result.fileName()).isEqualTo("clientes-resumido.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.content()).containsExactly((byte) 1, (byte) 2, (byte) 3);
        verify(customerReportGateway).generateSummaryReportPdf(command.toFilter());
    }

    @Test
    void shouldGenerateDetailedReportWhenFiltersAreValid() {
        var command = new GenerateCustomerReportCommand(
                CustomerReportType.DETAILED,
                null,
                null,
                null,
                null,
                null
        );

        when(customerReportGateway.generateDetailedReportPdf(command.toFilter())).thenReturn(new byte[]{7, 8});

        var result = useCase.execute(command);

        assertThat(result.fileName()).isEqualTo("clientes-detalhado.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.content()).containsExactly((byte) 7, (byte) 8);
        verify(customerReportGateway).generateDetailedReportPdf(command.toFilter());
    }

    @Test
    void shouldThrowWhenBirthMonthIsInvalid() {
        var command = new GenerateCustomerReportCommand(
                CustomerReportType.SUMMARY,
                null,
                null,
                13,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCustomerReportException.class)
                .hasMessage("Mês de aniversário deve estar entre 1 e 12.");

        verifyNoInteractions(customerReportGateway);
    }

    @Test
    void shouldThrowWhenBirthDateRangeIsInvalid() {
        var command = new GenerateCustomerReportCommand(
                CustomerReportType.SUMMARY,
                null,
                null,
                null,
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 1)
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCustomerReportException.class)
                .hasMessage("Data inicial de aniversário deve ser menor ou igual à data final.");

        verifyNoInteractions(customerReportGateway);
    }
}
