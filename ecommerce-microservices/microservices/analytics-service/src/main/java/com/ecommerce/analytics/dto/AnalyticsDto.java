package com.ecommerce.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class AnalyticsDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMetrics {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Long totalUsers;
        private Long totalProducts;
        private BigDecimal averageOrderValue;
        private Double conversionRate;
        private Double revenueGrowth;
        private Double orderGrowth;
        private Double userGrowth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesAnalytics {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
        private Map<String, Long> topSellingProducts;
        private Map<String, BigDecimal> revenueByDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnalytics {
        private Long totalUsers;
        private Long newUsers;
        private Long activeUsers;
        private Double userRetentionRate;
        private Map<String, Double> userDemographics;
        private Double userGrowthRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAnalytics {
        private Long totalProducts;
        private Long activeProducts;
        private Long outOfStockProducts;
        private Long lowStockProducts;
        private Map<String, Double> topRatedProducts;
        private Map<String, Map<String, Object>> categoryPerformance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAnalytics {
        private Long totalOrders;
        private Long completedOrders;
        private Long pendingOrders;
        private Long cancelledOrders;
        private Map<String, Long> orderStatusDistribution;
        private BigDecimal averageOrderValue;
        private Map<String, Long> ordersByDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueAnalytics {
        private BigDecimal totalRevenue;
        private Map<String, BigDecimal> revenueByMonth;
        private Map<String, BigDecimal> revenueByCategory;
        private Double revenueGrowthRate;
        private BigDecimal averageRevenuePerOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsRequest {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String queryType;
        private Map<String, Object> parameters;
    }
}
