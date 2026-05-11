package com.ecommerce.review.service;

import com.ecommerce.review.dto.ReviewDto;
import com.ecommerce.review.entity.*;
import com.ecommerce.review.exception.ReviewException;
import com.ecommerce.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public ReviewDto createReview(ReviewDto.CreateReviewRequest request) {
        log.info("Creating review for product: {} by user: {}", request.getProductId(), request.getUserId());

        // Check if user has already reviewed this product
        boolean hasReviewed = reviewRepository.existsByProductIdAndUserId(request.getProductId(), request.getUserId());
        if (hasReviewed) {
            throw new ReviewException("User has already reviewed this product");
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .productSku(request.getProductSku())
                .productName(request.getProductName())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .isVerifiedPurchase(request.getIsVerifiedPurchase())
                .isApproved(false) // Reviews need approval
                .reviewDate(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Add images if provided
        if (request.getImageUrls() != null) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .build();
                savedReview.addImage(image);
            }
        }

        Review finalReview = reviewRepository.save(savedReview);

        // Publish review created event
        com.ecommerce.events.dto.ReviewEvent event = com.ecommerce.events.dto.ReviewEvent.builder()
                .reviewId(finalReview.getId())
                .productId(finalReview.getProductId())
                .userId(finalReview.getUserId())
                .userName(finalReview.getUserName())
                .rating(finalReview.getRating())
                .title(finalReview.getTitle())
                .content(finalReview.getContent())
                .build();
        kafkaEventProducer.publishReviewCreatedEvent(event);

        log.info("Review created successfully with ID: {}", finalReview.getId());
        return convertToDto(finalReview);
    }

    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));
        return convertToDto(review);
    }

    public Page<ReviewDto> getProductReviews(Long productId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return reviewRepository.findByProductIdAndIsApproved(productId, true, pageable)
                .map(this::convertToDto);
    }

    public Page<ReviewDto> getUserReviews(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    public Page<ReviewDto> getAllReviews(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return reviewRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<ReviewDto> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return reviewRepository.findByIsApproved(false, pageable)
                .map(this::convertToDto);
    }

    public ReviewDto updateReview(Long id, ReviewDto.UpdateReviewRequest request) {
        log.info("Updating review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated successfully with ID: {}", updatedReview.getId());
        return convertToDto(updatedReview);
    }

    public ReviewDto approveReview(Long id) {
        log.info("Approving review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));

        review.setIsApproved(true);
        Review updatedReview = reviewRepository.save(review);

        // Publish review approved event
        com.ecommerce.events.dto.ReviewEvent event = com.ecommerce.events.dto.ReviewEvent.builder()
                .reviewId(updatedReview.getId())
                .productId(updatedReview.getProductId())
                .userId(updatedReview.getUserId())
                .userName(updatedReview.getUserName())
                .rating(updatedReview.getRating())
                .title(updatedReview.getTitle())
                .content(updatedReview.getContent())
                .build();
        kafkaEventProducer.publishReviewApprovedEvent(event);

        log.info("Review approved successfully with ID: {}", updatedReview.getId());
        return convertToDto(updatedReview);
    }

    public void deleteReview(Long id) {
        log.info("Deleting review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));

        reviewRepository.delete(review);
        log.info("Review deleted successfully with ID: {}", id);
    }

    public ReviewDto markHelpful(Long id) {
        log.info("Marking review as helpful: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));

        review.incrementHelpfulCount();
        Review updatedReview = reviewRepository.save(review);
        
        log.info("Review marked as helpful: {}", id);
        return convertToDto(updatedReview);
    }

    public ReviewDto markNotHelpful(Long id) {
        log.info("Marking review as not helpful: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException("Review not found: " + id));

        review.incrementNotHelpfulCount();
        Review updatedReview = reviewRepository.save(review);
        
        log.info("Review marked as not helpful: {}", id);
        return convertToDto(updatedReview);
    }

    public ReviewDto addResponse(Long reviewId, ReviewDto.AddResponseRequest request) {
        log.info("Adding response to review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("Review not found: " + reviewId));

        ReviewResponse response = ReviewResponse.builder()
                .review(review)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .userRole(request.getUserRole())
                .content(request.getContent())
                .build();

        review.addResponse(response);
        Review updatedReview = reviewRepository.save(review);
        
        log.info("Response added to review: {}", reviewId);
        return convertToDto(updatedReview);
    }

    public ReviewDto getProductRatingSummary(Long productId) {
        log.info("Getting rating summary for product: {}", productId);
        
        // Get rating statistics
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.getReviewCountByProductId(productId);
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistributionByProductId(productId);
        
        // Build rating summary
        ReviewDto.RatingSummary.RatingSummaryBuilder summaryBuilder = ReviewDto.RatingSummary.builder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L);
        
        // Process rating distribution
        for (Object[] ratingData : ratingDistribution) {
            Integer rating = (Integer) ratingData[0];
            Long count = (Long) ratingData[1];
            
            switch (rating) {
                case 5:
                    summaryBuilder.fiveStarCount(count);
                    break;
                case 4:
                    summaryBuilder.fourStarCount(count);
                    break;
                case 3:
                    summaryBuilder.threeStarCount(count);
                    break;
                case 2:
                    summaryBuilder.twoStarCount(count);
                    break;
                case 1:
                    summaryBuilder.oneStarCount(count);
                    break;
            }
        }
        
        ReviewDto.RatingSummary summary = summaryBuilder.build();
        
        return ReviewDto.builder()
                .productId(productId)
                .ratingSummary(summary)
                .build();
    }

    private ReviewDto convertToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .productSku(review.getProductSku())
                .productName(review.getProductName())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .userEmail(review.getUserEmail())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .isApproved(review.getIsApproved())
                .helpfulCount(review.getHelpfulCount())
                .notHelpfulCount(review.getNotHelpfulCount())
                .reviewDate(review.getReviewDate())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(review.getImages().stream()
                        .map(image -> ReviewDto.ReviewImageDto.builder()
                                .id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .imageCaption(image.getImageCaption())
                                .isPrimary(image.getIsPrimary())
                                .createdAt(image.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .responses(review.getResponses().stream()
                        .map(response -> ReviewDto.ReviewResponseDto.builder()
                                .id(response.getId())
                                .userId(response.getUserId())
                                .userName(response.getUserName())
                                .userRole(response.getUserRole())
                                .content(response.getContent())
                                .createdAt(response.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
