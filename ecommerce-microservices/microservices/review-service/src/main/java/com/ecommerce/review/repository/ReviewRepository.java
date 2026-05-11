package com.ecommerce.review.repository;

import com.ecommerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    Page<Review> findByProductIdAndIsApproved(Long productId, Boolean isApproved, Pageable pageable);
    
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    Page<Review> findByIsApproved(Boolean isApproved, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE (:rating IS NULL OR r.rating = :rating) " +
           "AND (:isVerified IS NULL OR r.isVerifiedPurchase = :isVerified) " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<Review> searchReviews(@Param("rating") Integer rating,
                           @Param("isVerified") Boolean isVerified,
                           @Param("startDate") java.time.LocalDateTime startDate,
                           @Param("endDate") java.time.LocalDateTime endDate,
                           Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Long getReviewCountByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true GROUP BY r.rating")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.isApproved = false")
    Long countPendingReviews();
    
    @Query("SELECT r FROM Review r WHERE r.rating >= 4 AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findTopRatedReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.isVerifiedPurchase = true AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findVerifiedPurchaseReviews(Pageable pageable);
}
