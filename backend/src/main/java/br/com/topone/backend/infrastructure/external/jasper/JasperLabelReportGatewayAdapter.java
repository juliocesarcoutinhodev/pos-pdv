package br.com.topone.backend.infrastructure.external.jasper;

import br.com.topone.backend.application.usecase.label.LabelReportGateway;
import br.com.topone.backend.domain.exception.LabelReportGenerationException;
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
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JasperLabelReportGatewayAdapter implements LabelReportGateway {

    private static final String REPORT_TEMPLATE_PATH = "reports/labels/gondola-label.jrxml";
    private static final String PARAM_JOB_ID = "JOB_ID";

    private final DataSource dataSource;
    private volatile JasperReport compiledReport;

    @Override
    public byte[] generateJobReportPdf(String jobId) {
        try (var connection = dataSource.getConnection()) {
            var report = getCompiledReport();
            var parameters = new HashMap<String, Object>();
            parameters.put(PARAM_JOB_ID, jobId);
            JasperPrint filled = JasperFillManager.fillReport(report, parameters, connection);
            return JasperExportManager.exportReportToPdf(filled);
        } catch (JRException | JRRuntimeException | java.sql.SQLException ex) {
            log.error("Failed to generate label print report | jobId={}", jobId, ex);
            throw new LabelReportGenerationException("Falha ao gerar relatório de etiquetas.", ex);
        }
    }

    private JasperReport getCompiledReport() {
        var report = compiledReport;
        if (report != null) {
            return report;
        }

        synchronized (this) {
            if (compiledReport == null) {
                compiledReport = compileReport();
            }
            return compiledReport;
        }
    }

    private JasperReport compileReport() {
        try (var template = getClass().getClassLoader().getResourceAsStream(REPORT_TEMPLATE_PATH)) {
            if (template == null) {
                throw new LabelReportGenerationException("Template de etiquetas não encontrado: " + REPORT_TEMPLATE_PATH);
            }
            return JasperCompileManager.compileReport(template);
        } catch (JRException | JRRuntimeException | IOException ex) {
            throw new LabelReportGenerationException("Falha ao compilar template de etiquetas.", ex);
        }
    }
}
