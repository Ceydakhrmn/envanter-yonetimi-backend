package com.example.demo.service;

import com.example.demo.entity.Invitation;
import com.example.demo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;

    @Override
    public Invitation createInvitation(String email, String role, Long expiresInMinutes) {
        if (invitationRepository.existsByEmailAndUsedFalse(email)) {
            throw new IllegalArgumentException("Aktif bir davet zaten mevcut.");
        }
        Invitation invitation = new Invitation();
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setEmail(email);
        invitation.setRole(role != null ? role : "USER");
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusMinutes(expiresInMinutes != null ? expiresInMinutes : 60));
        invitation.setUsed(false);
        return invitationRepository.save(invitation);
    }

    @Override
    public Optional<Invitation> verifyToken(String token) {
        return invitationRepository.findByToken(token)
                .filter(inv -> !inv.getUsed() && inv.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Override
    public boolean acceptInvitation(String token) {
        Optional<Invitation> invitationOpt = verifyToken(token);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            invitation.setUsed(true);
            invitationRepository.save(invitation);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsActiveInvitation(String email) {
        return invitationRepository.existsByEmailAndUsedFalse(email);
    }
}
