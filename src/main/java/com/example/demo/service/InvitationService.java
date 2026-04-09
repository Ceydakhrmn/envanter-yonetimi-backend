package com.example.demo.service;

import com.example.demo.entity.Invitation;
import com.example.demo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public Invitation createInvitation(String email, String role) {
        Invitation invitation = new Invitation();
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setEmail(email);
        invitation.setRole(role != null ? role : "USER");
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setUsed(false);
        Invitation saved = invitationRepository.save(invitation);

        String inviteLink = frontendUrl + "/invite/" + saved.getToken();
        try {
            emailService.sendInvitationEmail(
                email,
                "Efsora'ya Davet Edildiniz",
                buildEmailBody(inviteLink)
            );
            log.info("Davet emaili gönderildi: {}", email);
        } catch (Exception e) {
            log.warn("Davet emaili gönderilemedi ({}): {}", email, e.getMessage());
        }

        return saved;
    }

    private String buildEmailBody(String inviteLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Efsora'ya Davet Edildiniz</h2>
                <p>Merhaba,</p>
                <p>Efsora platformuna katılmaya davet edildiniz. Hesabınızı oluşturmak için aşağıdaki butona tıklayın.</p>
                <p style="margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #6366f1; color: white; padding: 12px 24px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                        Hesabı Oluştur
                    </a>
                </p>
                <p style="color: #888; font-size: 14px;">Bu link 24 saat geçerlidir.</p>
                <p style="color: #888; font-size: 12px;">Butona tıklayamazsanız şu linki kopyalayın: %s</p>
            </div>
            """.formatted(inviteLink, inviteLink);
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
