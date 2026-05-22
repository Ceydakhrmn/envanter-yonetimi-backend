package com.example.demo.service;

import com.example.demo.entity.Invitation;
import com.example.demo.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Invitation Service Tests")
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvitationService invitationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("Pending invitations should receive a single reminder")
    void sendPendingInvitationReminders_sendsReminderAndMarksInvitation() {
        Invitation invitation = new Invitation();
        invitation.setEmail("invitee@efsora.com");
        invitation.setToken("token-123");
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(2));

        when(invitationRepository.findByUsedFalseAndReminderSentAtIsNullAndExpiresAtBetween(any(), any()))
                .thenReturn(new java.util.ArrayList<>(List.of(invitation)));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString())).thenReturn(true);

        List<Invitation> result = invitationService.sendPendingInvitationReminders();

        assertThat(result).containsExactly(invitation);
        assertThat(invitation.getReminderSentAt()).isNotNull();
        verify(emailService, times(1)).sendInvitationEmail(anyString(), anyString(), anyString());
        verify(invitationRepository, times(1)).saveAll(result);
    }

    @Test
    @DisplayName("Failed reminder delivery should not mark invitation")
    void sendPendingInvitationReminders_skipsMarkingWhenDeliveryFails() {
        Invitation invitation = new Invitation();
        invitation.setEmail("invitee@efsora.com");
        invitation.setToken("token-123");
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(2));

        when(invitationRepository.findByUsedFalseAndReminderSentAtIsNullAndExpiresAtBetween(any(), any()))
                .thenReturn(new java.util.ArrayList<>(List.of(invitation)));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString())).thenReturn(false);

        List<Invitation> result = invitationService.sendPendingInvitationReminders();

        assertThat(result).isEmpty();
        assertThat(invitation.getReminderSentAt()).isNull();
        verify(invitationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Expired invitations should receive a single expiration notification")
    void sendExpiredInvitationNotifications_marksNotifiedInvitations() {
        Invitation invitation = new Invitation();
        invitation.setEmail("invitee@efsora.com");
        invitation.setToken("token-123");
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(invitationRepository.findByUsedFalseAndExpirationNotifiedAtIsNullAndExpiresAtBefore(any()))
                .thenReturn(new java.util.ArrayList<>(List.of(invitation)));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString())).thenReturn(true);

        List<Invitation> result = invitationService.sendExpiredInvitationNotifications();

        assertThat(result).containsExactly(invitation);
        assertThat(invitation.getExpirationNotifiedAt()).isNotNull();
        verify(invitationRepository, times(1)).saveAll(result);
    }
}