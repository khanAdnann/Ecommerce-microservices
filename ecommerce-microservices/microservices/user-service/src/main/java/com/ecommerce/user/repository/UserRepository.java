package com.ecommerce.user.repository;

import com.ecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Page<User> findAllActiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    Page<User> findAllInactiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :threshold")
    Page<User> findUnverifiedUsersOlderThan(@Param("threshold") LocalDateTime threshold, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countUsersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.email LIKE %:name%")
    Page<User> searchUsers(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findUsersByRole(@Param("role") User.Role role, Pageable pageable);
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByResetToken(String token);
    
    @Query("SELECT u FROM User u WHERE u.resetTokenExpiry < :now")
    Page<User> findUsersWithExpiredResetTokens(@Param("now") LocalDateTime now, Pageable pageable);
}
