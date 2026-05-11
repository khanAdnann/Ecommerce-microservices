package com.ecommerce.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    
    private String eventId;
    private String eventType;
    private String metricType;
    private BigDecimal metricValue;
    private Map<String, Object> dimensions;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public enum MetricType {
        REVENUE, ORDERS, USERS, PRODUCTS, CONVERSION_RATE, AVERAGE_ORDER_VALUE
    }

    public enum AnalyticsEventType {
        METRIC_RECORDED, REPORT_GENERATED, DASHBOARD_UPDATED, ALERT_TRIGGERED
    }
}
