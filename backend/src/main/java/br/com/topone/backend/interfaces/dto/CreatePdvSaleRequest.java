package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreatePdvSaleRequest(
        @NotNull PdvPaymentMethod paymentMethod,
        @DecimalMin(value = "0.00") BigDecimal discountAmount,
        @DecimalMin(value = "0.00") BigDecimal paidAmount,
        @Size(max = 255) String notes,
        @Valid @NotEmpty List<CreatePdvSaleItemRequest> items
) {
}

