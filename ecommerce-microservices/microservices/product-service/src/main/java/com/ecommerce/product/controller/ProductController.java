package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details by ID")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves product details by SKU")
    public ResponseEntity<ProductDto> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        ProductDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves paginated list of all products")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (default: asc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Page<ProductDto> products = productService.getAllProducts(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active products", description = "Retrieves paginated list of active products")
    public ResponseEntity<Page<ProductDto>> getActiveProducts(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (default: asc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Page<ProductDto> products = productService.getActiveProducts(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieves paginated list of featured products")
    public ResponseEntity<Page<ProductDto>> getFeaturedProducts(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by query string")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.searchProducts(query, page, size);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced product search", description = "Advanced product search with multiple filters")
    public ResponseEntity<Page<ProductDto>> advancedSearch(@Valid @RequestBody ProductDto.ProductSearchRequest searchRequest) {
        Page<ProductDto> products = productService.advancedSearch(searchRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieves products in a specific category")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (default: asc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Page<ProductDto> products = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Retrieves products within a price range")
    public ResponseEntity<Page<ProductDto>> getProductsByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.getProductsByPriceRange(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Retrieves products by brand")
    public ResponseEntity<Page<ProductDto>> getProductsByBrand(
            @Parameter(description = "Brand name") @PathVariable String brand,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.getProductsByBrand(brand, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated products", description = "Retrieves products sorted by rating")
    public ResponseEntity<Page<ProductDto>> getTopRatedProducts(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.getTopRatedProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest products", description = "Retrieves latest products sorted by creation date")
    public ResponseEntity<Page<ProductDto>> getLatestProducts(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductDto> products = productService.getLatestProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(summary = "Create new product", description = "Creates a new product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto.CreateProductRequest request) {
        log.info("Creating new product: {}", request.getSku());
        ProductDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductDto.UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);
        ProductDto product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update product status", description = "Updates product status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProductStatus(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Product status") @RequestParam ProductDto.ProductStatus status) {
        log.info("Updating product status for ID: {} to {}", id, status);
        ProductDto product = productService.updateProductStatus(id, com.ecommerce.product.entity.Product.ProductStatus.valueOf(status.name()));
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/brands")
    @Operation(summary = "Get all brands", description = "Retrieves list of all distinct brands")
    public ResponseEntity<List<String>> getDistinctBrands() {
        List<String> brands = productService.getDistinctBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/count")
    @Operation(summary = "Get active product count", description = "Returns count of active products")
    public ResponseEntity<Long> getActiveProductCount() {
        long count = productService.getActiveProductCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/category/{categoryId}/count")
    @Operation(summary = "Get product count by category", description = "Returns count of active products in a category")
    public ResponseEntity<Long> getProductCountByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        long count = productService.getProductCountByCategory(categoryId);
        return ResponseEntity.ok(count);
    }
}
