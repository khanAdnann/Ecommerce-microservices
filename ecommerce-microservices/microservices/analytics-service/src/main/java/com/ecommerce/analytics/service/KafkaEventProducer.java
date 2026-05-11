package com.ecommerce.analytics.service;

import com.ecommerce.events.dto.AnalyticsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishMetricRecordedEvent(String metricType, BigDecimal metricValue, Map<String, Object> dimensions) {
        try {
            AnalyticsEvent event = AnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("METRIC_RECORDED")
                    .metricType(metricType)
                    .metricValue(metricValue)
                    .dimensions(dimensions)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("analytics-events", eventJson);
            
            log.info("Published metric recorded event for metric: {}", metricType);
        } catch (JsonProcessingException e) {
            log.error("Error publishing metric recorded event", e);
        }
    }

    public void publishDashboardUpdatedEvent(Map<String, Object> dashboardData) {
        try {
            AnalyticsEvent event = AnalyticsEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("DASHBOARD_UPDATED")
                    .metricType(AnalyticsEvent.MetricType.REVENUE.name())
                    .dimensions(dashboardData)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("analytics-events", eventJson);
            
            log.info("Published dashboard updated event");
        } catch (JsonProcessingException e) {
            log.error("Error publishing dashboard updated event", e);
        }
    }
}
