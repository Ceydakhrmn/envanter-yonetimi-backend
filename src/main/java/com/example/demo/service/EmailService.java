package com.example.demo.service;

public interface EmailService {
    void sendInvitationEmail(String to, String subject, String body);
}
