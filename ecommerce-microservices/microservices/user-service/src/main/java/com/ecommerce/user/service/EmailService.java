package com.ecommerce.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email Address");
            
            String emailContent = buildVerificationEmailContent(verificationUrl);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            
            String emailContent = buildPasswordResetEmailContent(resetUrl);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    private String buildVerificationEmailContent(String verificationUrl) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Welcome to E-Commerce Platform!</h2>
                <p>Thank you for registering with us. Please click the link below to verify your email address:</p>
                <p>
                    <a href="%s" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Verify Email Address
                    </a>
                </p>
                <p>If the button above doesn't work, you can copy and paste the following link into your browser:</p>
                <p>%s</p>
                <p>This link will expire in 24 hours.</p>
                <p>If you didn't create an account with us, please ignore this email.</p>
                <hr style="margin: 20px 0; border: none; border-top: 1px solid #eee;">
                <p style="color: #666; font-size: 12px;">
                    © 2023 E-Commerce Platform. All rights reserved.
                </p>
            </div>
            """.formatted(verificationUrl, verificationUrl);
    }

    private String buildPasswordResetEmailContent(String resetUrl) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Reset Your Password</h2>
                <p>We received a request to reset your password for your E-Commerce Platform account.</p>
                <p>Click the link below to reset your password:</p>
                <p>
                    <a href="%s" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Reset Password
                    </a>
                </p>
                <p>If the button above doesn't work, you can copy and paste the following link into your browser:</p>
                <p>%s</p>
                <p>This link will expire in 1 hour for security reasons.</p>
                <p>If you didn't request a password reset, please ignore this email. Your password will remain unchanged.</p>
                <hr style="margin: 20px 0; border: none; border-top: 1px solid #eee;">
                <p style="color: #666; font-size: 12px;">
                    © 2023 E-Commerce Platform. All rights reserved.
                </p>
            </div>
            """.formatted(resetUrl, resetUrl);
    }

    public void sendOrderConfirmationEmail(String to, String orderNumber, BigDecimal totalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Order Confirmation - " + orderNumber);
            
            String emailContent = buildOrderConfirmationEmailContent(orderNumber, totalAmount);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email to: {}", to, e);
        }
    }

    private String buildOrderConfirmationEmailContent(String orderNumber, BigDecimal totalAmount) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Order Confirmed!</h2>
                <p>Thank you for your order. Your order has been successfully placed.</p>
                <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3 style="margin: 0 0 10px 0;">Order Details:</h3>
                    <p style="margin: 5px 0;"><strong>Order Number:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Total Amount:</strong> $%.2f</p>
                </div>
                <p>You will receive another email when your order ships.</p>
                <p>
                    <a href="%s/orders" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        View Order Details
                    </a>
                </p>
                <hr style="margin: 20px 0; border: none; border-top: 1px solid #eee;">
                <p style="color: #666; font-size: 12px;">
                    © 2023 E-Commerce Platform. All rights reserved.
                </p>
            </div>
            """.formatted(orderNumber, totalAmount, frontendUrl);
    }
}
