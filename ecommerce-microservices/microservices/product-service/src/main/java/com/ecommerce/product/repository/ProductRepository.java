package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);
    
    Page<Product> findByFeaturedTrue(Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.featured = :featured")
    Page<Product> findByStatusAndFeatured(@Param("status") Product.ProductStatus status, 
                                         @Param("featured") Boolean featured, 
                                         Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND p.status = :status")
    Page<Product> searchProducts(@Param("query") String query, 
                                @Param("status") Product.ProductStatus status, 
                                Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice " +
           "AND p.status = :status")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice, 
                                   @Param("status") Product.ProductStatus status, 
                                   Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.rating >= :minRating AND p.status = :status")
    Page<Product> findByMinRating(@Param("minRating") BigDecimal minRating, 
                                 @Param("status") Product.ProductStatus status, 
                                 Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
           "AND p.price BETWEEN :minPrice AND :maxPrice " +
           "AND p.status = :status")
    Page<Product> findByCategoryAndPriceRange(@Param("categoryId") Long categoryId, 
                                              @Param("minPrice") BigDecimal minPrice, 
                                              @Param("maxPrice") BigDecimal maxPrice, 
                                              @Param("status") Product.ProductStatus status, 
                                              Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.attributes a " +
           "WHERE a.attributeName = :attributeName AND a.attributeValue = :attributeValue " +
           "AND p.status = :status")
    Page<Product> findByAttribute(@Param("attributeName") String attributeName, 
                                  @Param("attributeValue") String attributeValue, 
                                  @Param("status") Product.ProductStatus status, 
                                  Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.status = :status")
    Page<Product> findByBrand(@Param("brand") String brand, 
                              @Param("status") Product.ProductStatus status, 
                              Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.rating DESC")
    Page<Product> findTopRatedProducts(@Param("status") Product.ProductStatus status, 
                                       Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Product> findLatestProducts(@Param("status") Product.ProductStatus status, 
                                     Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countByStatus(@Param("status") Product.ProductStatus status);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = :status")
    long countByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                  @Param("status") Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND (:query IS NULL OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brand IS NULL OR p.brand = :brand) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR p.rating >= :minRating) " +
           "AND (:featured IS NULL OR p.featured = :featured)")
    Page<Product> advancedSearch(@Param("query") String query,
                                 @Param("categoryId") Long categoryId,
                                 @Param("brand") String brand,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("minRating") BigDecimal minRating,
                                 @Param("featured") Boolean featured,
                                 @Param("status") Product.ProductStatus status,
                                 Pageable pageable);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.status = :status AND p.brand IS NOT NULL")
    List<String> findDistinctBrands(@Param("status") Product.ProductStatus status);
}
