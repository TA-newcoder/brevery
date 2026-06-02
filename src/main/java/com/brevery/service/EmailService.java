package com.brevery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@brevery.com}")
    private String fromEmail;

    /**
     * Gửi email xác thực tài khoản (async).
     */
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String subject = "Xác thực tài khoản Brevery";
        String verifyUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
        String body = buildVerificationEmailBody(fullName, verifyUrl);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Gửi email đặt lại mật khẩu (async).
     */
    @Async
    public void sendResetPasswordEmail(String toEmail, String fullName, String token) {
        String subject = "Đặt lại mật khẩu Brevery";
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String body = buildResetPasswordEmailBody(fullName, resetUrl);
        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildVerificationEmailBody(String fullName, String verifyUrl) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:0 auto;padding:20px;">
                  <h2 style="color:#D4875A;">🍞 Brevery — Xác thực email</h2>
                  <p>Xin chào <strong>%s</strong>,</p>
                  <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn nút bên dưới để kích hoạt:</p>
                  <a href="%s"
                     style="display:inline-block;background:#D4875A;color:#fff;padding:12px 28px;
                            border-radius:12px;text-decoration:none;font-weight:bold;margin:16px 0;">
                     Xác thực tài khoản
                  </a>
                  <p style="color:#8C7B74;font-size:13px;">Link có hiệu lực trong 24 giờ.</p>
                </div>
                """.formatted(fullName, verifyUrl);
    }

    private String buildResetPasswordEmailBody(String fullName, String resetUrl) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:0 auto;padding:20px;">
                  <h2 style="color:#D4875A;">🔒 Brevery — Đặt lại mật khẩu</h2>
                  <p>Xin chào <strong>%s</strong>,</p>
                  <p>Bạn đã yêu cầu đặt lại mật khẩu. Nhấn nút bên dưới:</p>
                  <a href="%s"
                     style="display:inline-block;background:#D4875A;color:#fff;padding:12px 28px;
                            border-radius:12px;text-decoration:none;font-weight:bold;margin:16px 0;">
                     Đặt lại mật khẩu
                  </a>
                  <p style="color:#8C7B74;font-size:13px;">Link có hiệu lực trong 1 giờ. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
                </div>
                """.formatted(fullName, resetUrl);
    }
}
