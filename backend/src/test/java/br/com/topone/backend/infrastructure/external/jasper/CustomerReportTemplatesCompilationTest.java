package br.com.topone.backend.infrastructure.external.jasper;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerReportTemplatesCompilationTest {

    private static final String SUMMARY_TEMPLATE_PATH = "reports/customers/customers-summary.jrxml";
    private static final String DETAILED_TEMPLATE_PATH = "reports/customers/customers-detailed.jrxml";

    @Test
    void shouldCompileSummaryTemplate() throws Exception {
        compileTemplate(SUMMARY_TEMPLATE_PATH);
    }

    @Test
    void shouldCompileDetailedTemplate() throws Exception {
        compileTemplate(DETAILED_TEMPLATE_PATH);
    }

    private void compileTemplate(String templatePath) throws Exception {
        try (var template = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            assertThat(template).as("Template not found: %s", templatePath).isNotNull();
            JasperCompileManager.compileReport(template);
        }
    }
}
