package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.ProductException;
import com.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "#id")
    public ProductDto.CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductException("Category not found: " + id));
        return convertToDto(category);
    }

    @Cacheable(value = "categories", key = "'all'")
    public List<ProductDto.CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categories", key = "'root'")
    public List<ProductDto.CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categories", key = "'root-with-children'")
    public List<ProductDto.CategoryDto> getRootCategoriesWithChildren() {
        return categoryRepository.findRootCategoriesWithChildren().stream()
                .map(this::convertToDtoWithChildren)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categories", key = "'children-' + #parentId")
    public List<ProductDto.CategoryDto> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "categories", allEntries = true)
    public ProductDto.CategoryDto createCategory(ProductDto.CategoryDto categoryDto) {
        log.info("Creating new category: {}", categoryDto.getName());

        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ProductException("Category already exists: " + categoryDto.getName());
        }

        Category category = Category.builder()
                .name(categoryDto.getName())
                .description(categoryDto.getDescription())
                .imageUrl(categoryDto.getImageUrl())
                .build();

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ProductException("Parent category not found: " + categoryDto.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return convertToDto(savedCategory);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public ProductDto.CategoryDto updateCategory(Long id, ProductDto.CategoryDto categoryDto) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductException("Category not found: " + id));

        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
        if (categoryDto.getDescription() != null) {
            category.setDescription(categoryDto.getDescription());
        }
        if (categoryDto.getImageUrl() != null) {
            category.setImageUrl(categoryDto.getImageUrl());
        }
        if (categoryDto.getParentId() != null) {
            if (categoryDto.getParentId().equals(id)) {
                throw new ProductException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ProductException("Parent category not found: " + categoryDto.getParentId()));
            category.setParent(parent);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return convertToDto(updatedCategory);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findCategoryWithChildren(id)
                .orElseThrow(() -> new ProductException("Category not found: " + id));

        if (!category.getChildren().isEmpty()) {
            throw new ProductException("Cannot delete category with child categories");
        }

        long productCount = categoryRepository.countActiveProductsByCategory(id);
        if (productCount > 0) {
            throw new ProductException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    public long getProductCountByCategory(Long categoryId) {
        return categoryRepository.countActiveProductsByCategory(categoryId);
    }

    private ProductDto.CategoryDto convertToDto(Category category) {
        return ProductDto.CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .imageUrl(category.getImageUrl())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private ProductDto.CategoryDto convertToDtoWithChildren(Category category) {
        return ProductDto.CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .imageUrl(category.getImageUrl())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(category.getChildren().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
