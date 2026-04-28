package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.pdv.*;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pdv")
@RequiredArgsConstructor
public class PdvManagementController {

    private final OpenCashRegisterUseCase openCashRegisterUseCase;
    private final GetCurrentCashRegisterUseCase getCurrentCashRegisterUseCase;
    private final CreateCashMovementUseCase createCashMovementUseCase;
    private final LookupPdvProductUseCase lookupPdvProductUseCase;
    private final CreatePdvSaleUseCase createPdvSaleUseCase;
    private final ListRecentPdvSalesUseCase listRecentPdvSalesUseCase;
    private final ListOpenCashRegistersForMonitoringUseCase listOpenCashRegistersForMonitoringUseCase;
    private final GetCashRegisterMonitoringSummaryUseCase getCashRegisterMonitoringSummaryUseCase;

    @GetMapping("/cash/current")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<CashRegisterStatusResponse> getCurrentCash() {
        var result = getCurrentCashRegisterUseCase.execute(currentUser().getId());
        return ResponseEntity.ok(toCashStatusResponse(result));
    }

    @PostMapping("/cash/open")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<CashRegisterStatusResponse> openCash(@Valid @RequestBody OpenCashRegisterRequest request) {
        var result = openCashRegisterUseCase.execute(new OpenCashRegisterCommand(
                currentUser().getId(),
                request.openingAmount()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toCashStatusResponse(result));
    }

    @PostMapping("/cash/movements")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<CashMovementResponse> createCashMovement(@Valid @RequestBody CreateCashMovementRequest request) {
        var result = createCashMovementUseCase.execute(new CreateCashMovementCommand(
                currentUser().getId(),
                request.type(),
                request.amount(),
                request.note()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(new CashMovementResponse(
                result.id(),
                result.type(),
                result.amount(),
                result.note(),
                result.createdAt(),
                result.cashBalance()
        ));
    }

    @GetMapping("/products/lookup")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PdvProductLookupResponse> lookupProduct(@RequestParam String code) {
        var result = lookupPdvProductUseCase.execute(new PdvProductLookupCommand(code));
        return ResponseEntity.ok(new PdvProductLookupResponse(
                result.id(),
                result.sku(),
                result.barcode(),
                result.name(),
                result.unit(),
                result.unitPrice(),
                result.stockQuantity()
        ));
    }

    @PostMapping("/sales")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PdvSaleResponse> createSale(@Valid @RequestBody CreatePdvSaleRequest request) {
        var result = createPdvSaleUseCase.execute(new CreatePdvSaleCommand(
                currentUser().getId(),
                request.paymentMethod(),
                request.discountAmount(),
                request.paidAmount(),
                request.notes(),
                request.items().stream()
                        .map(item -> new CreatePdvSaleItemCommand(item.productId(), item.quantity()))
                        .toList()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toSaleResponse(result));
    }

    @GetMapping("/sales/recent")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<java.util.List<PdvSaleResponse>> listRecentSales(@RequestParam(defaultValue = "10") int limit) {
        var sales = listRecentPdvSalesUseCase.execute(currentUser().getId(), limit).stream()
                .map(this::toSaleResponse)
                .toList();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/monitor/open-cash-registers")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<java.util.List<OpenCashRegisterMonitorResponse>> listOpenCashRegistersForMonitoring() {
        var result = listOpenCashRegistersForMonitoringUseCase.execute().stream()
                .map(item -> new OpenCashRegisterMonitorResponse(
                        item.sessionId(),
                        item.userId(),
                        item.userName(),
                        item.status(),
                        item.openedAt(),
                        item.cashBalance()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monitor/open-cash-registers/{sessionId}/summary")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<CashRegisterMonitoringSummaryResponse> getOpenCashRegisterSummary(
            @PathVariable java.util.UUID sessionId
    ) {
        var result = getCashRegisterMonitoringSummaryUseCase.execute(sessionId);
        return ResponseEntity.ok(new CashRegisterMonitoringSummaryResponse(
                result.sessionId(),
                result.userId(),
                result.userName(),
                result.status(),
                result.openedAt(),
                result.openingAmount(),
                result.suppliesAmount(),
                result.withdrawalsAmount(),
                result.salesAmount(),
                result.cashBalance(),
                result.salesCount(),
                result.paymentSummary().stream()
                        .map(item -> new CashRegisterPaymentSummaryResponse(
                                item.paymentMethod(),
                                item.totalAmount(),
                                item.salesCount()
                        ))
                        .toList(),
                result.recentSales().stream()
                        .map(item -> new CashRegisterSaleSummaryResponse(
                                item.id(),
                                item.paymentMethod(),
                                item.totalAmount(),
                                item.paidAmount(),
                                item.changeAmount(),
                                item.createdAt(),
                                item.itemsCount()
                        ))
                        .toList()
        ));
    }

    private CashRegisterStatusResponse toCashStatusResponse(CashRegisterStatusResult result) {
        return new CashRegisterStatusResponse(
                result.sessionId(),
                result.openingAmount(),
                result.openedAt(),
                result.suppliesAmount(),
                result.withdrawalsAmount(),
                result.salesAmount(),
                result.cashBalance()
        );
    }

    private PdvSaleResponse toSaleResponse(PdvSaleResult result) {
        return new PdvSaleResponse(
                result.id(),
                result.paymentMethod(),
                result.subtotalAmount(),
                result.discountAmount(),
                result.totalAmount(),
                result.paidAmount(),
                result.changeAmount(),
                result.createdAt(),
                result.items().stream()
                        .map(item -> new PdvSaleItemResponse(
                                item.productId(),
                                item.sku(),
                                item.barcode(),
                                item.name(),
                                item.unit(),
                                item.unitPrice(),
                                item.quantity(),
                                item.lineTotal()
                        ))
                        .toList()
        );
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
