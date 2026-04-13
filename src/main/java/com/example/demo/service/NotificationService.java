package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public Notification create(String type, String message, String userEmail) {
        Notification n = new Notification();
        n.setType(type);
        n.setMessage(message);
        n.setUserEmail(userEmail);
        Notification saved = notificationRepository.save(n);
        sendEmailAsync(userEmail, type, message);
        return saved;
    }

    public void notifyAllAdmins(String type, String message, List<String> adminEmails) {
        for (String email : adminEmails) {
            create(type, message, email);
        }
    }

    @Async
    protected void sendEmailAsync(String to, String type, String message) {
        try {
            emailService.sendNotificationEmail(to, type, message);
        } catch (Exception e) {
            log.warn("Bildirim e-postası gönderilemedi {}: {}", to, e.getMessage());
        }
    }

    public List<Notification> getByUser(String userEmail) {
        return notificationRepository.findTop50ByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<Notification> getUnread(String userEmail) {
        return notificationRepository.findByUserEmailAndReadFalseOrderByCreatedAtDesc(userEmail);
    }

    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserEmailAndReadFalse(userEmail);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        List<Notification> unread = notificationRepository
                .findByUserEmailAndReadFalseOrderByCreatedAtDesc(userEmail);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteAll(String userEmail) {
        notificationRepository.deleteByUserEmail(userEmail);
    }
}
