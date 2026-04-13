package br.com.topone.backend.infrastructure.external.jasper;

import br.com.topone.backend.application.usecase.customer.CustomerReportFilter;
import br.com.topone.backend.application.usecase.customer.CustomerReportGateway;
import br.com.topone.backend.domain.exception.CustomerReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
public class JasperCustomerReportGatewayAdapter implements CustomerReportGateway {

    private static final String SUMMARY_TEMPLATE_PATH = "reports/customers/customers-summary.jrxml";
    private static final String DETAILED_TEMPLATE_PATH = "reports/customers/customers-detailed.jrxml";

    private static final String PARAM_NAME_FILTER = "NAME_FILTER";
    private static final String PARAM_ACTIVE_FILTER = "ACTIVE_FILTER";
    private static final String PARAM_BIRTH_MONTH = "BIRTH_MONTH";
    private static final String PARAM_BIRTH_DATE_FROM = "BIRTH_DATE_FROM";
    private static final String PARAM_BIRTH_DATE_TO = "BIRTH_DATE_TO";
    private static final String PARAM_COMPANY_DISPLAY_NAME = "COMPANY_DISPLAY_NAME";
    private static final String PARAM_COMPANY_TAX_ID = "COMPANY_TAX_ID";
    private static final String PARAM_COMPANY_CONTACT = "COMPANY_CONTACT";
    private static final String PARAM_COMPANY_ADDRESS = "COMPANY_ADDRESS";

    private final DataSource dataSource;
    private final CustomerReportCompanyProperties companyProperties;
    private volatile JasperReport summaryCompiledReport;
    private volatile JasperReport detailedCompiledReport;

    @Override
    public byte[] generateSummaryReportPdf(CustomerReportFilter filter) {
        return generateReportPdf(getSummaryCompiledReport(), filter, "summary");
    }

    @Override
    public byte[] generateDetailedReportPdf(CustomerReportFilter filter) {
        return generateReportPdf(getDetailedCompiledReport(), filter, "detailed");
    }

    private byte[] generateReportPdf(JasperReport report, CustomerReportFilter filter, String reportType) {
        try (var connection = dataSource.getConnection()) {
            var parameters = buildParameters(filter);
            JasperPrint filled = JasperFillManager.fillReport(report, parameters, connection);
            return JasperExportManager.exportReportToPdf(filled);
        } catch (JRException | JRRuntimeException | SQLException ex) {
            log.error("Failed to generate customer report | type={} | filters={}", reportType, filter, ex);
            throw new CustomerReportGenerationException("Falha ao gerar relatório de clientes.", ex);
        }
    }

    private HashMap<String, Object> buildParameters(CustomerReportFilter filter) {
        var parameters = new HashMap<String, Object>();

        // Jasper SQL parameters are always filled explicitly to support report generation without filters.
        parameters.put(PARAM_NAME_FILTER, normalize(filter.nameFilter()));
        parameters.put(PARAM_ACTIVE_FILTER, filter.activeFilter());
        parameters.put(PARAM_BIRTH_MONTH, filter.birthMonth());
        parameters.put(PARAM_BIRTH_DATE_FROM, toSqlDate(filter.birthDateFrom()));
        parameters.put(PARAM_BIRTH_DATE_TO, toSqlDate(filter.birthDateTo()));

        parameters.put(PARAM_COMPANY_DISPLAY_NAME, resolveCompanyDisplayName());
        parameters.put(PARAM_COMPANY_TAX_ID, normalize(companyProperties.getTaxId()));
        parameters.put(PARAM_COMPANY_CONTACT, resolveCompanyContact());
        parameters.put(PARAM_COMPANY_ADDRESS, normalize(companyProperties.getAddressLine()));

        return parameters;
    }

    private Date toSqlDate(java.time.LocalDate value) {
        if (value == null) {
            return null;
        }
        return Date.valueOf(value);
    }

    private String resolveCompanyDisplayName() {
        var tradeName = normalize(companyProperties.getTradeName());
        if (tradeName != null) {
            return tradeName;
        }
        return normalize(companyProperties.getLegalName());
    }

    private String resolveCompanyContact() {
        var contact = new StringJoiner(" | ");
        var phone = normalize(companyProperties.getPhone());
        var email = normalize(companyProperties.getEmail());

        if (phone != null) {
            contact.add("Tel: " + phone);
        }
        if (email != null) {
            contact.add("E-mail: " + email);
        }

        var value = contact.toString();
        return value.isBlank() ? null : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private JasperReport getSummaryCompiledReport() {
        var report = summaryCompiledReport;
        if (report != null) {
            return report;
        }

        synchronized (this) {
            if (summaryCompiledReport == null) {
                summaryCompiledReport = compileReport(SUMMARY_TEMPLATE_PATH);
            }
            return summaryCompiledReport;
        }
    }

    private JasperReport getDetailedCompiledReport() {
        var report = detailedCompiledReport;
        if (report != null) {
            return report;
        }

        synchronized (this) {
            if (detailedCompiledReport == null) {
                detailedCompiledReport = compileReport(DETAILED_TEMPLATE_PATH);
            }
            return detailedCompiledReport;
        }
    }

    private JasperReport compileReport(String templatePath) {
        try (var template = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (template == null) {
                throw new CustomerReportGenerationException("Template de relatório não encontrado: " + templatePath);
            }
            return JasperCompileManager.compileReport(template);
        } catch (JRException | JRRuntimeException | IOException ex) {
            throw new CustomerReportGenerationException("Falha ao compilar template de relatório de clientes.", ex);
        }
    }
}
