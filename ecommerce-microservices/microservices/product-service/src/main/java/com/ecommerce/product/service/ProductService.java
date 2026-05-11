package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductAttribute;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.ProductException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Cacheable(value = "products", key = "#sku")
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductException("Product not found with sku: " + sku));
        return convertToDto(product);
    }

    public Page<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return productRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getActiveProducts(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("featured").descending());
        return productRepository.findByFeaturedTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> searchProducts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        return productRepository.searchProducts(query, Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> advancedSearch(ProductDto.ProductSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "name";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "asc";
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return productRepository.advancedSearch(
                searchRequest.getQuery(),
                searchRequest.getCategoryId(),
                searchRequest.getBrand(),
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice(),
                searchRequest.getMinRating(),
                searchRequest.getFeatured(),
                Product.ProductStatus.ACTIVE,
                pageable
        ).map(this::convertToDto);
    }

    public Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findByPriceRange(minPrice, maxPrice, Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getProductsByBrand(String brand, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findByBrand(brand, Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getTopRatedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        return productRepository.findTopRatedProducts(Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getLatestProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findLatestProducts(Product.ProductStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto createProduct(ProductDto.CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductException("Product with SKU already exists: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ProductException("Category not found: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .category(category)
                .brand(request.getBrand())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .color(request.getColor())
                .size(request.getSize())
                .material(request.getMaterial())
                .status(Product.ProductStatus.ACTIVE)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .build();

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            product.setImagesArray(request.getImages().toArray(new String[0]));
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            product.setTagsArray(request.getTags().toArray(new String[0]));
        }

        Product savedProduct = productRepository.save(product);

        // Add attributes if provided
        if (request.getAttributes() != null) {
            for (ProductDto.ProductAttributeDto attrDto : request.getAttributes()) {
                ProductAttribute attribute = ProductAttribute.builder()
                        .product(savedProduct)
                        .attributeName(attrDto.getAttributeName())
                        .attributeValue(attrDto.getAttributeValue())
                        .build();
                savedProduct.addAttribute(attribute);
            }
        }

        Product finalProduct = productRepository.save(savedProduct);

        // Publish product created event
        kafkaEventProducer.publishProductCreatedEvent(finalProduct);

        log.info("Product created successfully with ID: {}", finalProduct.getId());
        return convertToDto(finalProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto updateProduct(Long id, ProductDto.UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found: " + id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ProductException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getSize() != null) {
            product.setSize(request.getSize());
        }
        if (request.getMaterial() != null) {
            product.setMaterial(request.getMaterial());
        }
        if (request.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(request.getStatus().name()));
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
        if (request.getImages() != null) {
            product.setImagesArray(request.getImages().toArray(new String[0]));
        }
        if (request.getTags() != null) {
            product.setTagsArray(request.getTags().toArray(new String[0]));
        }

        // Update attributes if provided
        if (request.getAttributes() != null) {
            // Clear existing attributes
            product.getAttributes().clear();
            
            // Add new attributes
            for (ProductDto.ProductAttributeDto attrDto : request.getAttributes()) {
                ProductAttribute attribute = ProductAttribute.builder()
                        .product(product)
                        .attributeName(attrDto.getAttributeName())
                        .attributeValue(attrDto.getAttributeValue())
                        .build();
                product.addAttribute(attribute);
            }
        }

        Product updatedProduct = productRepository.save(product);

        // Publish product updated event
        kafkaEventProducer.publishProductUpdatedEvent(updatedProduct);

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return convertToDto(updatedProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found: " + id));

        productRepository.delete(product);

        // Publish product deleted event
        kafkaEventProducer.publishProductDeletedEvent(product);

        log.info("Product deleted successfully with ID: {}", id);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto updateProductStatus(Long id, Product.ProductStatus status) {
        log.info("Updating product status for ID: {} to {}", id, status);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found: " + id));

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);

        // Publish product status updated event
        kafkaEventProducer.publishProductStatusUpdatedEvent(updatedProduct);

        log.info("Product status updated successfully for ID: {}", id);
        return convertToDto(updatedProduct);
    }

    public List<String> getDistinctBrands() {
        return productRepository.findDistinctBrands(Product.ProductStatus.ACTIVE);
    }

    public long getActiveProductCount() {
        return productRepository.countByStatus(Product.ProductStatus.ACTIVE);
    }

    public long getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryAndStatus(categoryId, Product.ProductStatus.ACTIVE);
    }

    private ProductDto convertToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .category(product.getCategory() != null ? 
                        ProductDto.CategoryDto.builder()
                                .id(product.getCategory().getId())
                                .name(product.getCategory().getName())
                                .description(product.getCategory().getDescription())
                                .parentId(product.getCategory().getParent() != null ? product.getCategory().getParent().getId() : null)
                                .imageUrl(product.getCategory().getImageUrl())
                                .build()
                        : null)
                .brand(product.getBrand())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .color(product.getColor())
                .size(product.getSize())
                .material(product.getMaterial())
                .images(Arrays.asList(product.getImagesArray()))
                .tags(Arrays.asList(product.getTagsArray()))
                .status(ProductDto.ProductStatus.valueOf(product.getStatus().name()))
                .featured(product.getFeatured())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .attributes(product.getAttributes().stream()
                        .map(attr -> ProductDto.ProductAttributeDto.builder()
                                .id(attr.getId())
                                .attributeName(attr.getAttributeName())
                                .attributeValue(attr.getAttributeValue())
                                .createdAt(attr.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
