package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationReminderScheduler {

    private final InvitationService invitationService;

    @Scheduled(cron = "${app.invitations.reminder-cron:0 0 * * * *}")
    public void processInvitationNotifications() {
        int reminders = invitationService.sendPendingInvitationReminders().size();
        int expirations = invitationService.sendExpiredInvitationNotifications().size();

        if (reminders > 0 || expirations > 0) {
            log.info("Invitation notification rules processed. reminders={}, expirations={}", reminders, expirations);
        }
    }
}