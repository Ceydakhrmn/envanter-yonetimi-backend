package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(String type, String message, String userEmail) {
        Notification n = new Notification();
        n.setType(type);
        n.setMessage(message);
        n.setUserEmail(userEmail);
        return notificationRepository.save(n);
    }

    public void notifyAllAdmins(String type, String message, List<String> adminEmails) {
        for (String email : adminEmails) {
            create(type, message, email);
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
