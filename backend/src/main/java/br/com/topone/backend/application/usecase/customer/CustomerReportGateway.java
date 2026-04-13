package br.com.topone.backend.application.usecase.customer;

public interface CustomerReportGateway {

    byte[] generateSummaryReportPdf(CustomerReportFilter filter);

    byte[] generateDetailedReportPdf(CustomerReportFilter filter);
}
