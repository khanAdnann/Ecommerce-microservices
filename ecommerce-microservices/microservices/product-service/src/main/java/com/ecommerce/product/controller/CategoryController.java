package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves category details by ID")
    public ResponseEntity<ProductDto.CategoryDto> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        ProductDto.CategoryDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves list of all categories")
    public ResponseEntity<List<ProductDto.CategoryDto>> getAllCategories() {
        List<ProductDto.CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieves categories without parent")
    public ResponseEntity<List<ProductDto.CategoryDto>> getRootCategories() {
        List<ProductDto.CategoryDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root-with-children")
    @Operation(summary = "Get root categories with children", description = "Retrieves root categories with their children")
    public ResponseEntity<List<ProductDto.CategoryDto>> getRootCategoriesWithChildren() {
        List<ProductDto.CategoryDto> categories = categoryService.getRootCategoriesWithChildren();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get child categories", description = "Retrieves child categories of a parent")
    public ResponseEntity<List<ProductDto.CategoryDto>> getChildCategories(
            @Parameter(description = "Parent category ID") @PathVariable Long parentId) {
        List<ProductDto.CategoryDto> categories = categoryService.getChildCategories(parentId);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @Operation(summary = "Create new category", description = "Creates a new category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto.CategoryDto> createCategory(@Valid @RequestBody ProductDto.CategoryDto categoryDto) {
        log.info("Creating new category: {}", categoryDto.getName());
        ProductDto.CategoryDto category = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto.CategoryDto> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody ProductDto.CategoryDto categoryDto) {
        log.info("Updating category with ID: {}", id);
        ProductDto.CategoryDto category = categoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/product-count")
    @Operation(summary = "Get product count by category", description = "Returns count of active products in category")
    public ResponseEntity<Long> getProductCountByCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        long count = categoryService.getProductCountByCategory(id);
        return ResponseEntity.ok(count);
    }
}
