package br.com.topone.backend.domain.exception;

import java.util.UUID;

public class LabelPrintJobNotFoundException extends RuntimeException {

    public LabelPrintJobNotFoundException() {
        super("Lote de impressão não encontrado");
    }

    public LabelPrintJobNotFoundException(UUID id) {
        super("Lote de impressão não encontrado: " + id);
    }
}
