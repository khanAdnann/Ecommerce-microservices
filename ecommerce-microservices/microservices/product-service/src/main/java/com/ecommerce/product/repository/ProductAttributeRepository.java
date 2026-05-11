package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    
    List<ProductAttribute> findByProductId(Long productId);
    
    @Query("SELECT DISTINCT pa.attributeName FROM ProductAttribute pa")
    List<String> findDistinctAttributeNames();
    
    @Query("SELECT DISTINCT pa.attributeValue FROM ProductAttribute pa WHERE pa.attributeName = :attributeName")
    List<String> findDistinctAttributeValuesByName(String attributeName);
    
    void deleteByProductId(Long productId);
}
