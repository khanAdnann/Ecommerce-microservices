package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.dto.AnalyticsDto;
import com.ecommerce.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics Management", description = "Analytics management APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard metrics", description = "Retrieves dashboard metrics and KPIs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.DashboardMetrics> getDashboardMetrics() {
        log.info("Fetching dashboard metrics");
        AnalyticsDto.DashboardMetrics metrics = analyticsService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/sales")
    @Operation(summary = "Get sales analytics", description = "Retrieves sales analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.SalesAnalytics> getSalesAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching sales analytics from {} to {}", startDate, endDate);
        AnalyticsDto.SalesAnalytics analytics = analyticsService.getSalesAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/users")
    @Operation(summary = "Get user analytics", description = "Retrieves user analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.UserAnalytics> getUserAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching user analytics from {} to {}", startDate, endDate);
        AnalyticsDto.UserAnalytics analytics = analyticsService.getUserAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/products")
    @Operation(summary = "Get product analytics", description = "Retrieves product analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.ProductAnalytics> getProductAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching product analytics from {} to {}", startDate, endDate);
        AnalyticsDto.ProductAnalytics analytics = analyticsService.getProductAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get order analytics", description = "Retrieves order analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.OrderAnalytics> getOrderAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching order analytics from {} to {}", startDate, endDate);
        AnalyticsDto.OrderAnalytics analytics = analyticsService.getOrderAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics", description = "Retrieves revenue analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto.RevenueAnalytics> getRevenueAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching revenue analytics from {} to {}", startDate, endDate);
        AnalyticsDto.RevenueAnalytics analytics = analyticsService.getRevenueAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/custom")
    @Operation(summary = "Get custom analytics", description = "Retrieves custom analytics based on query parameters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomAnalytics(@RequestBody AnalyticsDto.AnalyticsRequest request) {
        log.info("Fetching custom analytics for query type: {}", request.getQueryType());
        Map<String, Object> analytics = analyticsService.getCustomAnalytics(request.getQueryType(), request.getParameters());
        return ResponseEntity.ok(analytics);
    }
}
