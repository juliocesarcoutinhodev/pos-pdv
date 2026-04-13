package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.InvalidCustomerReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateCustomerReportUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final CustomerReportGateway customerReportGateway;

    @Transactional(readOnly = true)
    public GenerateCustomerReportResult execute(GenerateCustomerReportCommand command) {
        validateFilters(command);

        var content = switch (command.reportType()) {
            case SUMMARY -> customerReportGateway.generateSummaryReportPdf(command.toFilter());
            case DETAILED -> customerReportGateway.generateDetailedReportPdf(command.toFilter());
        };

        var fileName = switch (command.reportType()) {
            case SUMMARY -> "clientes-resumido.pdf";
            case DETAILED -> "clientes-detalhado.pdf";
        };

        return new GenerateCustomerReportResult(fileName, PDF_CONTENT_TYPE, content);
    }

    private void validateFilters(GenerateCustomerReportCommand command) {
        if (command.birthMonth() != null && (command.birthMonth() < 1 || command.birthMonth() > 12)) {
            throw new InvalidCustomerReportException("Mês de aniversário deve estar entre 1 e 12.");
        }

        if (command.birthDateFrom() != null
                && command.birthDateTo() != null
                && command.birthDateFrom().isAfter(command.birthDateTo())) {
            throw new InvalidCustomerReportException("Data inicial de aniversário deve ser menor ou igual à data final.");
        }
    }
}
