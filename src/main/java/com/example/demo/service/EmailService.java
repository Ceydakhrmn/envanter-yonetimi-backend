package com.example.demo.service;

public interface EmailService {
    boolean sendInvitationEmail(String to, String subject, String body);
    boolean sendNotificationEmail(String to, String type, String message);
}
