package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.ProductFilter;
import br.com.topone.backend.domain.repository.ProductRepository;
import br.com.topone.backend.domain.repository.SortDirection;
import br.com.topone.backend.infrastructure.persistence.entity.ProductEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.ProductJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.ProductMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository, ProductMapper mapper) {
        this.productJpaRepository = productJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity;
        if (product.getId() != null) {
            entity = productJpaRepository.findById(product.getId())
                    .orElseThrow(() -> new ProductNotFoundException(product.getId()));
            mapper.updateEntity(product, entity);
        } else {
            entity = mapper.toEntity(product);
        }

        var saved = productJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return productJpaRepository.existsBySku(sku);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return productJpaRepository.existsByBarcode(barcode);
    }

    @Override
    public Optional<Product> findBySkuExcludingId(String sku, UUID excludeId) {
        return productJpaRepository.findBySkuExcludingId(sku, excludeId).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByBarcodeExcludingId(String barcode, UUID excludeId) {
        return productJpaRepository.findByBarcodeExcludingId(barcode, excludeId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> findAll(ProductFilter filter, int page, int size, PageSort sort) {
        var namePattern = filter.name() != null && !filter.name().isBlank()
                ? "%" + filter.name().toLowerCase() + "%" : null;
        var skuPattern = filter.sku() != null && !filter.sku().isBlank()
                ? "%" + Product.normalizeSku(filter.sku()) + "%" : null;
        var barcodePattern = filter.barcode() != null && !filter.barcode().isBlank()
                ? "%" + Product.normalizeBarcode(filter.barcode()) + "%" : null;
        var categoryPattern = filter.category() != null && !filter.category().isBlank()
                ? "%" + filter.category().toLowerCase() + "%" : null;

        var sortSpec = sort != null && sort.isSorted()
                ? Sort.by(
                sort.direction() == SortDirection.DESC
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sort.field())
                : Sort.unsorted();

        var springPage = PageRequest.of(page, size, sortSpec);
        var pageResult = productJpaRepository.searchByFilter(
                namePattern,
                skuPattern,
                barcodePattern,
                categoryPattern,
                filter.active(),
                springPage
        );

        var products = pageResult.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(
                products,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
}
