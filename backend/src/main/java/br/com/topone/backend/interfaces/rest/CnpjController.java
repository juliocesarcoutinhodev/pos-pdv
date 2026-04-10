package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.cnpj.CnpjLookupResult;
import br.com.topone.backend.application.usecase.cnpj.CnpjLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for CNPJ lookup operations.
 */
@RestController
@RequestMapping("/api/v1/cnpj")
@RequiredArgsConstructor
public class CnpjController {

    private final CnpjLookupUseCase cnpjLookupUseCase;

    @GetMapping
    public ResponseEntity<CnpjLookupResult> lookup(@RequestParam String taxId) {
        return ResponseEntity.ok(cnpjLookupUseCase.execute(taxId));
    }
}

