package com.example.demo.service;

import com.example.demo.entity.Invitation;
import com.example.demo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;

    public Invitation createInvitation(String email, String role) {
        Invitation invitation = new Invitation();
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setEmail(email);
        invitation.setRole(role != null ? role : "USER");
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setUsed(false);
        return invitationRepository.save(invitation);
    }

    public Invitation findByToken(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
    }

    public boolean isValid(Invitation invitation) {
        return !invitation.getUsed() && invitation.getExpiresAt().isAfter(LocalDateTime.now());
    }

    public void markAsUsed(Invitation invitation) {
        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }
}
