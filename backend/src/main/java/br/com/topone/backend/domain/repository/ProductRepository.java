package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findActiveBySku(String sku);

    Optional<Product> findActiveByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    Optional<Product> findBySkuExcludingId(String sku, UUID excludeId);

    Optional<Product> findByBarcodeExcludingId(String barcode, UUID excludeId);

    PageResult<Product> findAll(ProductFilter filter, int page, int size, PageSort sort);

    long countActiveProducts();

    long countLowStockProducts();
}
