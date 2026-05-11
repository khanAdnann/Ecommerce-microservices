package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.exception.UserException;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserDto registerUser(UserDto.CreateUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .verificationToken(UUID.randomUUID().toString())
                .build();

        user.addRole(User.Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());

        // Publish user registered event to Kafka
        kafkaEventProducer.publishUserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName());

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return convertToDto(savedUser);
    }

    public UserDto.LoginResponse loginUser(UserDto.LoginRequest request) {
        log.info("Logging in user with email: {}", request.getEmail());

        // Load user and verify credentials manually
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new UserException("User account is not verified");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Publish user login event to Kafka
        kafkaEventProducer.publishUserLoginEvent(user.getId(), user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User logged in successfully with ID: {}", user.getId());
        
        return UserDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(convertToDto(user))
                .build();
    }

    public UserDto.LoginResponse refreshToken(UserDto.RefreshTokenRequest request) {
        log.info("Refreshing token for user");

        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UserException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));

        if (!user.isEnabled()) {
            throw new UserException("User account is disabled");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return UserDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(convertToDto(user))
                .build();
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));
        return convertToDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));
        return convertToDto(user);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public UserDto updateUser(Long id, UserDto.UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User updatedUser = userRepository.save(user);

        // Publish user updated event to Kafka
        kafkaEventProducer.publishUserUpdatedEvent(updatedUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), updatedUser.getLastName());

        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return convertToDto(updatedUser);
    }

    public void changePassword(Long userId, UserDto.ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    public void forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, resetToken);

        log.info("Password reset token sent to email: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new UserException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for user ID: {}", user.getId());
    }

    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UserException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified successfully for user ID: {}", user.getId());
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));

        String userEmail = user.getEmail();
        userRepository.delete(user);

        // Publish user deleted event to Kafka
        kafkaEventProducer.publishUserDeletedEvent(id, userEmail);

        log.info("User deleted successfully with ID: {}", id);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .roles(user.getRoles())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
