package com.ecommerce.review.service;

import com.ecommerce.events.dto.ReviewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishReviewCreatedEvent(ReviewEvent review) {
        try {
            ReviewEvent event = ReviewEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("REVIEW_CREATED")
                    .reviewId(review.getReviewId())
                    .productId(review.getProductId())
                    .userId(review.getUserId())
                    .userName(review.getUserName())
                    .rating(review.getRating())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("review-events", eventJson);
            
            log.info("Published review created event for product: {}", review.getProductId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing review created event", e);
        }
    }

    public void publishReviewApprovedEvent(ReviewEvent review) {
        try {
            ReviewEvent event = ReviewEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("REVIEW_APPROVED")
                    .reviewId(review.getReviewId())
                    .productId(review.getProductId())
                    .userId(review.getUserId())
                    .userName(review.getUserName())
                    .rating(review.getRating())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("review-events", eventJson);
            
            log.info("Published review approved event for product: {}", review.getProductId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing review approved event", e);
        }
    }
}
