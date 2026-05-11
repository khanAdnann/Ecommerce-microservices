package com.ecommerce.review.controller;

import com.ecommerce.review.dto.ReviewDto;
import com.ecommerce.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create review", description = "Creates a new product review")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto.CreateReviewRequest request,
                                                HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        request.setUserId(userId);
        
        log.info("Creating review for product: {}", request.getProductId());
        ReviewDto review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieves review details by ID")
    public ResponseEntity<ReviewDto> getReviewById(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Retrieves reviews for a specific product")
    public ResponseEntity<Page<ReviewDto>> getProductReviews(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user reviews", description = "Retrieves reviews for the authenticated user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ReviewDto>> getUserReviews(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        Page<ReviewDto> reviews = reviewService.getUserReviews(userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Retrieves all reviews (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<ReviewDto> reviews = reviewService.getAllReviews(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending reviews", description = "Retrieves reviews pending approval (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDto>> getPendingReviews(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewDto> reviews = reviewService.getPendingReviews(page, size);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Updates an existing review")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody ReviewDto.UpdateReviewRequest request,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Updating review: {}", id);
        ReviewDto review = reviewService.updateReview(id, request);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve review", description = "Approves a review (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDto> approveReview(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        log.info("Approving review: {}", id);
        ReviewDto review = reviewService.approveReview(id);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Deletes a review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        log.info("Deleting review: {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/helpful")
    @Operation(summary = "Mark review as helpful", description = "Marks a review as helpful")
    public ResponseEntity<ReviewDto> markHelpful(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        log.info("Marking review as helpful: {}", id);
        ReviewDto review = reviewService.markHelpful(id);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{id}/not-helpful")
    @Operation(summary = "Mark review as not helpful", description = "Marks a review as not helpful")
    public ResponseEntity<ReviewDto> markNotHelpful(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        log.info("Marking review as not helpful: {}", id);
        ReviewDto review = reviewService.markNotHelpful(id);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{id}/responses")
    @Operation(summary = "Add response to review", description = "Adds a response to a review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDto> addResponse(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody ReviewDto.AddResponseRequest request) {
        log.info("Adding response to review: {}", id);
        ReviewDto review = reviewService.addResponse(id, request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}/rating-summary")
    @Operation(summary = "Get product rating summary", description = "Retrieves rating summary for a product")
    public ResponseEntity<ReviewDto> getProductRatingSummary(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        ReviewDto summary = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(summary);
    }
}
