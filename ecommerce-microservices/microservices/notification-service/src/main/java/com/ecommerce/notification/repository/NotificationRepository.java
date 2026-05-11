package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.createdAt < :threshold")
    List<Notification> findFailedNotificationsOlderThan(LocalDateTime threshold);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING'")
    List<Notification> findPendingNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    long countByStatus(Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :startDate AND n.createdAt <= :endDate")
    List<Notification> findNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
