package br.com.topone.backend.application.usecase.label;

public record GenerateLabelPrintJobReportResult(
        String fileName,
        String contentType,
        byte[] content
) {
}
