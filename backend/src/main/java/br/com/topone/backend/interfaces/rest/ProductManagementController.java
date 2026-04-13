package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.product.CreateProductUseCase;
import br.com.topone.backend.application.usecase.product.DeactivateProductCommand;
import br.com.topone.backend.application.usecase.product.DeactivateProductUseCase;
import br.com.topone.backend.application.usecase.product.GetProductByIdCommand;
import br.com.topone.backend.application.usecase.product.GetProductByIdResult;
import br.com.topone.backend.application.usecase.product.GetProductByIdUseCase;
import br.com.topone.backend.application.usecase.product.GetNextProductSkuUseCase;
import br.com.topone.backend.application.usecase.product.ListProductsCommand;
import br.com.topone.backend.application.usecase.product.ListProductsUseCase;
import br.com.topone.backend.application.usecase.product.UpdateProductResult;
import br.com.topone.backend.application.usecase.product.UpdateProductPatchUseCase;
import br.com.topone.backend.application.usecase.product.UpdateProductUseCase;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.ProductFilter;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.CreateProductRequest;
import br.com.topone.backend.interfaces.dto.DtoCommandMapper;
import br.com.topone.backend.interfaces.dto.NextProductSkuResponse;
import br.com.topone.backend.interfaces.dto.PageResponse;
import br.com.topone.backend.interfaces.dto.ProductDetailResponse;
import br.com.topone.backend.interfaces.dto.ProductListResponse;
import br.com.topone.backend.interfaces.dto.UpdateProductPatchRequest;
import br.com.topone.backend.interfaces.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductManagementController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "sku",
            "barcode",
            "category",
            "salePrice",
            "stockQuantity",
            "createdAt"
    );

    private final CreateProductUseCase createProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final GetNextProductSkuUseCase getNextProductSkuUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final UpdateProductPatchUseCase updateProductPatchUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final DtoCommandMapper dtoCommandMapper;

    @GetMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<PageResponse<ProductListResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var result = listProductsUseCase.execute(new ListProductsCommand(
                new ProductFilter(name, sku, barcode, category, active),
                page,
                size,
                PageSort.by(sortBy, sortDirection, ALLOWED_SORT_FIELDS)
        ));

        var content = result.content().stream()
                .map(product -> new ProductListResponse(
                        product.id(),
                        product.sku(),
                        product.barcode(),
                        product.name(),
                        product.category(),
                        product.supplierId(),
                        product.brand(),
                        product.unit(),
                        product.salePrice(),
                        product.stockQuantity(),
                        product.imageId(),
                        product.createdAt(),
                        product.updatedAt(),
                        product.active()
                ))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @GetMapping("/next-sku")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<NextProductSkuResponse> getNextSku() {
        var suggestedSku = getNextProductSkuUseCase.execute();
        return ResponseEntity.ok(new NextProductSkuResponse(suggestedSku));
    }

    @GetMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<ProductDetailResponse> getById(@PathVariable UUID id) {
        var result = getProductByIdUseCase.execute(new GetProductByIdCommand(id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PostMapping
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<ProductDetailResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var result = createProductUseCase.execute(dtoCommandMapper.toCreateProductCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductDetailResponse(
                result.id(),
                result.sku(),
                result.barcode(),
                result.name(),
                result.description(),
                result.brand(),
                result.category(),
                result.supplierId(),
                result.unit(),
                result.costPrice(),
                result.salePrice(),
                result.promotionalPrice(),
                result.stockQuantity(),
                result.minimumStock(),
                result.ncm(),
                result.cest(),
                result.cfop(),
                result.taxOrigin(),
                result.taxSituation(),
                result.icmsRate(),
                result.pisSituation(),
                result.pisRate(),
                result.cofinsSituation(),
                result.cofinsRate(),
                result.imageId(),
                result.createdAt(),
                null,
                true
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ProductDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        var result = updateProductUseCase.execute(dtoCommandMapper.toUpdateProductCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ProductDetailResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductPatchRequest request
    ) {
        var result = updateProductPatchUseCase.execute(dtoCommandMapper.toPatchProductCommand(request, id));
        return ResponseEntity.ok(toDetailResponse(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateProductUseCase.execute(new DeactivateProductCommand(id));
        return ResponseEntity.noContent().build();
    }

    private ProductDetailResponse toDetailResponse(GetProductByIdResult result) {
        return new ProductDetailResponse(
                result.id(),
                result.sku(),
                result.barcode(),
                result.name(),
                result.description(),
                result.brand(),
                result.category(),
                result.supplierId(),
                result.unit(),
                result.costPrice(),
                result.salePrice(),
                result.promotionalPrice(),
                result.stockQuantity(),
                result.minimumStock(),
                result.ncm(),
                result.cest(),
                result.cfop(),
                result.taxOrigin(),
                result.taxSituation(),
                result.icmsRate(),
                result.pisSituation(),
                result.pisRate(),
                result.cofinsSituation(),
                result.cofinsRate(),
                result.imageId(),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        );
    }

    private ProductDetailResponse toDetailResponse(UpdateProductResult result) {
        return new ProductDetailResponse(
                result.id(),
                result.sku(),
                result.barcode(),
                result.name(),
                result.description(),
                result.brand(),
                result.category(),
                result.supplierId(),
                result.unit(),
                result.costPrice(),
                result.salePrice(),
                result.promotionalPrice(),
                result.stockQuantity(),
                result.minimumStock(),
                result.ncm(),
                result.cest(),
                result.cfop(),
                result.taxOrigin(),
                result.taxSituation(),
                result.icmsRate(),
                result.pisSituation(),
                result.pisRate(),
                result.cofinsSituation(),
                result.cofinsRate(),
                result.imageId(),
                result.createdAt(),
                result.updatedAt(),
                result.active()
        );
    }
}
