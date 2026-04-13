package br.com.topone.backend.application.usecase.label;

public interface LabelReportGateway {

    byte[] generateJobReportPdf(String jobId);
}
