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

    @Override
    public void sendInvitationEmail(String to, String subject, String body) {
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("Mail yapılandırılmamış. Email gönderilemedi: {}", to);
            return;
        }
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
}
