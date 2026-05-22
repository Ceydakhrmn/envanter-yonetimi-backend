package com.example.demo.service;

import com.example.demo.entity.Invitation;
import com.example.demo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private static final long INVITATION_EXPIRY_HOURS = 24;
    private static final long REMINDER_LEAD_HOURS = 6;

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
        invitation.setExpiresAt(LocalDateTime.now().plusHours(INVITATION_EXPIRY_HOURS));
        invitation.setUsed(false);
        Invitation saved = invitationRepository.save(invitation);

        String inviteLink = frontendUrl + "/invite/" + saved.getToken();
        try {
            boolean sent = emailService.sendInvitationEmail(
                email,
                "Efsora'ya Davet Edildiniz",
                buildEmailBody(inviteLink)
            );
            if (sent) {
                log.info("Davet emaili gönderildi: {}", email);
            } else {
                log.warn("Davet emaili gönderilemedi: {}", email);
            }
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

    public boolean hasActiveInvitation(String email) {
        return invitationRepository.existsByEmailAndUsedFalse(email);
    }

    public boolean isValid(Invitation invitation) {
        return !invitation.getUsed() && invitation.getExpiresAt().isAfter(LocalDateTime.now());
    }

    public void markAsUsed(Invitation invitation) {
        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    @Transactional
    public List<Invitation> sendPendingInvitationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindowEnd = now.plusHours(REMINDER_LEAD_HOURS);
        List<Invitation> invitations = invitationRepository
                .findByUsedFalseAndReminderSentAtIsNullAndExpiresAtBetween(now, reminderWindowEnd);

        invitations.removeIf(invitation -> !sendReminderEmail(invitation, now));

        if (!invitations.isEmpty()) {
            invitationRepository.saveAll(invitations);
        }

        return invitations;
    }

    @Transactional
    public List<Invitation> sendExpiredInvitationNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Invitation> invitations = invitationRepository
                .findByUsedFalseAndExpirationNotifiedAtIsNullAndExpiresAtBefore(now);

        invitations.removeIf(invitation -> !sendExpirationEmail(invitation, now));

        if (!invitations.isEmpty()) {
            invitationRepository.saveAll(invitations);
        }

        return invitations;
    }

    private boolean sendReminderEmail(Invitation invitation, LocalDateTime now) {
        String inviteLink = frontendUrl + "/invite/" + invitation.getToken();
        long hoursRemaining = Math.max(0, ChronoUnit.HOURS.between(now, invitation.getExpiresAt()));

        try {
            boolean sent = emailService.sendInvitationEmail(
                    invitation.getEmail(),
                    "Efsora davetiniz için hatırlatma",
                    buildReminderEmailBody(inviteLink, hoursRemaining)
            );
            if (!sent) {
                return false;
            }
            invitation.setReminderSentAt(now);
            return true;
        } catch (Exception e) {
            log.warn("Davet hatırlatma emaili gönderilemedi ({}): {}", invitation.getEmail(), e.getMessage());
            return false;
        }
    }

    private boolean sendExpirationEmail(Invitation invitation, LocalDateTime now) {
        try {
            boolean sent = emailService.sendInvitationEmail(
                    invitation.getEmail(),
                    "Efsora davetinizin süresi doldu",
                    buildExpiredEmailBody()
            );
            if (!sent) {
                return false;
            }
            invitation.setExpirationNotifiedAt(now);
            return true;
        } catch (Exception e) {
            log.warn("Davet süresi dolma emaili gönderilemedi ({}): {}", invitation.getEmail(), e.getMessage());
            return false;
        }
    }

    private String buildReminderEmailBody(String inviteLink, long hoursRemaining) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Davetiniz Bekliyor</h2>
                <p>Merhaba,</p>
                <p>Efsora davetiniz henüz tamamlanmadı. Davet bağlantınızın süresi yaklaşık <strong>%d saat</strong> içinde dolacak.</p>
                <p style="margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #2563eb; color: white; padding: 12px 24px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                        Daveti Tamamla
                    </a>
                </p>
                <p style="color: #888; font-size: 12px;">Bağlantı açılmazsa şu linki kopyalayın: %s</p>
            </div>
            """.formatted(hoursRemaining, inviteLink, inviteLink);
    }

    private String buildExpiredEmailBody() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #333;">Davetinizin Süresi Doldu</h2>
                <p>Merhaba,</p>
                <p>Efsora için gönderilen davet bağlantınızın süresi doldu.</p>
                <p>Yeni bir davet bağlantısı almak için sistem yöneticinizle iletişime geçebilirsiniz.</p>
            </div>
            """;
    }
}
