package br.com.topone.backend.application.usecase.label;

import java.util.UUID;

public record GenerateLabelPrintJobReportCommand(UUID jobId) {
}
