package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.zip.ZipLookupResult;
import br.com.topone.backend.application.usecase.zip.ZipLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for ZIP code lookup operations.
 */
@RestController
@RequestMapping("/api/v1/zip")
@RequiredArgsConstructor
public class ZipController {

    private final ZipLookupUseCase zipLookupUseCase;

    @GetMapping
    public ResponseEntity<ZipLookupResult> lookup(@RequestParam String code) {
        return ResponseEntity.ok(zipLookupUseCase.execute(code));
    }
}

