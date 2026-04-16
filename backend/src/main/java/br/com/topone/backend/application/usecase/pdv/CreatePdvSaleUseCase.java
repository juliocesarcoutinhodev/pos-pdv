package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.CashRegisterSessionNotOpenException;
import br.com.topone.backend.domain.exception.InsufficientProductStockException;
import br.com.topone.backend.domain.exception.InvalidSaleException;
import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.domain.model.PdvSale;
import br.com.topone.backend.domain.model.PdvSaleItem;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CreatePdvSaleUseCase {

    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final ProductRepository productRepository;
    private final PdvSaleRepository pdvSaleRepository;

    @Transactional
    public PdvSaleResult execute(CreatePdvSaleCommand command) {
        var session = cashRegisterSessionRepository.findOpenByUserId(command.userId())
                .orElseThrow(CashRegisterSessionNotOpenException::new);

        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidSaleException("Adicione ao menos um item para finalizar a venda.");
        }

        if (command.paymentMethod() == null) {
            throw new InvalidSaleException("Informe a forma de pagamento da venda.");
        }

        var itemDomains = new ArrayList<PdvSaleItem>();
        for (var itemCommand : command.items()) {
            if (itemCommand.productId() == null) {
                throw new InvalidSaleException("Item da venda sem produto informado.");
            }

            var quantity = normalizeQuantity(itemCommand.quantity());
            if (quantity.signum() <= 0) {
                throw new InvalidSaleException("Quantidade do item deve ser maior que zero.");
            }

            var product = productRepository.findById(itemCommand.productId())
                    .orElseThrow(ProductNotFoundException::new);
            if (!product.isActive()) {
                throw new InvalidSaleException("Produto inativo não pode ser vendido.");
            }
            if (product.getStockQuantity().compareTo(quantity) < 0) {
                throw new InsufficientProductStockException(product.getId());
            }

            product.changeStockQuantity(product.getStockQuantity().subtract(quantity));
            product.touch();
            productRepository.save(product);

            itemDomains.add(PdvSaleItem.fromProduct(product, quantity));
        }

        var subtotal = itemDomains.stream()
                .map(PdvSaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        var discount = normalizeAmount(command.discountAmount());
        if (discount.signum() < 0) {
            throw new InvalidSaleException("Desconto da venda não pode ser negativo.");
        }
        var total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (total.signum() <= 0) {
            throw new InvalidSaleException("Valor total da venda deve ser maior que zero.");
        }

        var paidAmount = normalizeAmount(command.paidAmount());
        var changeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (PdvPaymentMethod.CASH.equals(command.paymentMethod())) {
            if (paidAmount.compareTo(total) < 0) {
                throw new InvalidSaleException("Valor recebido em dinheiro é menor que o total da venda.");
            }
            changeAmount = paidAmount.subtract(total).setScale(2, RoundingMode.HALF_UP);
        } else {
            paidAmount = total;
        }

        var sale = PdvSale.create(
                session.getId(),
                command.userId(),
                command.paymentMethod(),
                subtotal,
                discount,
                total,
                paidAmount,
                changeAmount,
                command.notes(),
                itemDomains
        );
        var saved = pdvSaleRepository.save(sale);

        return new PdvSaleResult(
                saved.getId(),
                saved.getPaymentMethod(),
                saved.getSubtotalAmount(),
                saved.getDiscountAmount(),
                saved.getTotalAmount(),
                saved.getPaidAmount(),
                saved.getChangeAmount(),
                saved.getCreatedAt(),
                saved.getItems().stream()
                        .map(item -> new PdvSaleItemResult(
                                item.getProductId(),
                                item.getSku(),
                                item.getBarcode(),
                                item.getName(),
                                item.getUnit(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineTotal()
                        ))
                        .toList()
        );
    }

    private BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

