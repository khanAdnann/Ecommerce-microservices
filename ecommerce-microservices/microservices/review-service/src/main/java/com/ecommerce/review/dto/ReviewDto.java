package com.ecommerce.review.dto;

import com.ecommerce.review.entity.Review;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private Long userId;
    private String userName;
    private String userEmail;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    private String title;
    private String content;
    private Boolean isVerifiedPurchase;
    private Boolean isApproved;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private LocalDateTime reviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<ReviewImageDto> images;
    private List<ReviewResponseDto> responses;
    private RatingSummary ratingSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        private String productSku;
        private String productName;
        
        @NotNull(message = "User ID is required")
        private Long userId;
        
        @NotBlank(message = "User name is required")
        private String userName;
        
        @NotBlank(message = "User email is required")
        private String userEmail;
        
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        private String title;
        private String content;
        private Boolean isVerifiedPurchase;
        private List<String> imageUrls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateReviewRequest {
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        private String title;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddResponseRequest {
        private Long userId;
        private String userName;
        private String userRole;
        
        @NotBlank(message = "Response content is required")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSearchRequest {
        private Long productId;
        private Integer rating;
        private Boolean isVerified;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortDirection;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewImageDto {
        private Long id;
        private String imageUrl;
        private String imageCaption;
        private Boolean isPrimary;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponseDto {
        private Long id;
        private Long userId;
        private String userName;
        private String userRole;
        private String content;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingSummary {
        private Double averageRating;
        private Long totalReviews;
        private Long fiveStarCount;
        private Long fourStarCount;
        private Long threeStarCount;
        private Long twoStarCount;
        private Long oneStarCount;
    }
}
