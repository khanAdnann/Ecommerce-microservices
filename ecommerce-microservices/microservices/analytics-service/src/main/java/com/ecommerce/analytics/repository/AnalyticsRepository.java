package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.AnalyticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsData, Long> {

    List<AnalyticsData> findByMetricTypeAndMetricDateBetweenOrderByMetricDateDesc(
            String metricType, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(ad.metricValue) FROM AnalyticsData ad WHERE ad.metricType = :metricType AND ad.metricDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumMetricValueByTypeAndDateRange(
            @Param("metricType") String metricType, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(ad) FROM AnalyticsData ad WHERE ad.metricType = :metricType AND ad.metricDate BETWEEN :startDate AND :endDate")
    Long countMetricsByTypeAndDateRange(
            @Param("metricType") String metricType, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(ad.metricValue) FROM AnalyticsData ad WHERE ad.metricType = :metricType AND ad.metricDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> avgMetricValueByTypeAndDateRange(
            @Param("metricType") String metricType, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    List<AnalyticsData> findTop10ByMetricTypeOrderByMetricDateDesc(String metricType);

    @Query("SELECT ad.metricType, COUNT(ad), SUM(ad.metricValue) FROM AnalyticsData ad WHERE ad.metricDate BETWEEN :startDate AND :endDate GROUP BY ad.metricType")
    List<Object[]> getMetricsSummaryByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
