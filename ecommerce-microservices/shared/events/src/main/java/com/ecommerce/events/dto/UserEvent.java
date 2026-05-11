package com.ecommerce.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    
    private String eventId;
    private String eventType;
    private Long userId;
    private String userName;
    private String userEmail;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public enum UserEventType {
        USER_REGISTERED, USER_UPDATED, USER_DELETED, EMAIL_VERIFIED, PASSWORD_CHANGED
    }
}
