package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private boolean isMailConfigured() {
        return mailSender != null && fromEmail != null && !fromEmail.isBlank();
    }

    @Override
    public void sendInvitationEmail(String to, String subject, String body) {
        if (!isMailConfigured()) {
            log.warn("Mail yapılandırılmamış. Email gönderilemedi: {}", to);
            return;
        }
        sendHtml(to, subject, body);
    }

    @Override
    public void sendNotificationEmail(String to, String type, String message) {
        if (!isMailConfigured()) {
            log.debug("Mail yapılandırılmamış. Bildirim e-postası atlandı: {}", to);
            return;
        }
        String iconColor = switch (type) {
            case "success" -> "#10b981";
            case "error" -> "#ef4444";
            case "warning" -> "#f59e0b";
            default -> "#3b82f6";
        };
        String typeLabel = switch (type) {
            case "success" -> "Başarılı";
            case "error" -> "Hata";
            case "warning" -> "Uyarı";
            default -> "Bilgi";
        };
        String html = """
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 500px; margin: 0 auto; padding: 24px;">
              <div style="background: #f8fafc; border-radius: 12px; padding: 24px; border-left: 4px solid %s;">
                <h3 style="margin: 0 0 8px; color: #1e293b; font-size: 16px;">Efsora — %s</h3>
                <p style="margin: 0; color: #475569; font-size: 14px; line-height: 1.6;">%s</p>
              </div>
              <div style="margin-top: 16px; text-align: center;">
                <a href="%s" style="display: inline-block; padding: 10px 24px; background: #3b82f6; color: white; text-decoration: none; border-radius: 8px; font-size: 14px;">Panele Git</a>
              </div>
              <p style="margin-top: 16px; color: #94a3b8; font-size: 12px; text-align: center;">Bu e-posta Efsora Envanter Yönetimi tarafından gönderilmiştir.</p>
            </div>
            """.formatted(iconColor, typeLabel, message, frontendUrl);

        try {
            sendHtml(to, "Efsora — " + typeLabel + ": " + truncate(message, 60), html);
        } catch (Exception e) {
            log.warn("Bildirim e-postası gönderilemedi {}: {}", to, e.getMessage());
        }
    }

    private void sendHtml(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email gönderildi: {}", to);
        } catch (MessagingException e) {
            log.error("Email gönderilemedi: {}", e.getMessage());
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
