package br.com.topone.backend.application.usecase.customer;

public record GenerateCustomerReportResult(
        String fileName,
        String contentType,
        byte[] content
) {
}
