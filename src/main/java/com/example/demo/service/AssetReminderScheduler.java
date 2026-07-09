package com.example.demo.service;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetMaintenanceSchedule;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetMaintenanceScheduleRepository;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetReminderScheduler {

    private final AssetRepository assetRepository;
    private final KullaniciRepository kullaniciRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AssetMaintenanceScheduleRepository maintenanceScheduleRepository;

    // Runs every day at 08:00 UTC
    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void checkAssetReminders() {
        List<String> adminEmails = kullaniciRepository
                .findByRoleAndActiveTrue(Kullanici.Role.ADMIN)
                .stream()
                .map(Kullanici::getEmail)
                .toList();

        if (adminEmails.isEmpty()) return;

        LocalDate today = LocalDate.now();

        checkRenewalReminders(today, adminEmails);
        checkWarrantyReminders(today, adminEmails);
        checkMaintenanceScheduleReminders(today, adminEmails);
    }

    private void checkRenewalReminders(LocalDate today, List<String> adminEmails) {
        int[] windows = {7, 14, 30};
        for (int days : windows) {
            LocalDate from = today.plusDays(days == 7 ? 0 : (days == 14 ? 7 : 14) + 1);
            LocalDate to = today.plusDays(days);
            List<Asset> assets = assetRepository.findByRenewalDateBetween(from, to);
            if (assets.isEmpty()) continue;

            String names = assets.stream().map(Asset::getName).limit(5)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            String suffix = assets.size() > 5 ? " ve " + (assets.size() - 5) + " daha" : "";
            String message = days + " gün içinde yenileme tarihi geliyor: " + names + suffix;

            for (String email : adminEmails) {
                notificationService.create("warning", message, email);
                emailService.sendNotificationEmail(email, "warning", message);
            }
            notificationService.broadcastWebSocketNotification("ASSET_RENEWAL", "Yenileme Hatırlatıcısı", message, "warning");
            log.info("Renewal reminder sent for {} assets ({}d window)", assets.size(), days);
        }
    }

    private void checkWarrantyReminders(LocalDate today, List<String> adminEmails) {
        LocalDate from = today;
        LocalDate to = today.plusDays(30);
        List<Asset> assets = assetRepository.findByWarrantyExpiryDateBetween(from, to);
        if (assets.isEmpty()) return;

        String names = assets.stream().map(Asset::getName).limit(5)
                .reduce((a, b) -> a + ", " + b).orElse("");
        String suffix = assets.size() > 5 ? " ve " + (assets.size() - 5) + " daha" : "";
        String message = "30 gün içinde garanti süresi bitiyor: " + names + suffix;

        for (String email : adminEmails) {
            notificationService.create("warning", message, email);
            emailService.sendNotificationEmail(email, "warning", message);
        }
        notificationService.broadcastWebSocketNotification("ASSET_WARRANTY", "Garanti Hatırlatıcısı", message, "warning");
        log.info("Warranty reminder sent for {} assets", assets.size());
    }

    private void checkMaintenanceScheduleReminders(LocalDate today, List<String> adminEmails) {
        // Mark overdue (PENDING past due date → OVERDUE)
        List<AssetMaintenanceSchedule> overdue = maintenanceScheduleRepository
                .findByStatusAndScheduledDateBefore("PENDING", today);
        overdue.forEach(s -> s.setStatus("OVERDUE"));
        if (!overdue.isEmpty()) {
            maintenanceScheduleRepository.saveAll(overdue);
            String names = overdue.stream().map(AssetMaintenanceSchedule::getAssetName).limit(5)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            String suffix = overdue.size() > 5 ? " ve " + (overdue.size() - 5) + " daha" : "";
            String msg = "Bakım planı gecikti: " + names + suffix;
            for (String email : adminEmails) {
                notificationService.create("error", msg, email);
                emailService.sendNotificationEmail(email, "error", msg);
            }
            log.info("Marked {} maintenance schedules as overdue", overdue.size());
        }

        // Remind for schedules due in 1–7 days
        List<AssetMaintenanceSchedule> upcoming = maintenanceScheduleRepository
                .findByStatusAndScheduledDateBetween("PENDING", today.plusDays(1), today.plusDays(7));
        if (upcoming.isEmpty()) return;

        String names = upcoming.stream().map(AssetMaintenanceSchedule::getAssetName).limit(5)
                .reduce((a, b) -> a + ", " + b).orElse("");
        String suffix = upcoming.size() > 5 ? " ve " + (upcoming.size() - 5) + " daha" : "";
        String message = "Yaklaşan bakım planı (7 gün içinde): " + names + suffix;

        for (String email : adminEmails) {
            notificationService.create("warning", message, email);
            emailService.sendNotificationEmail(email, "warning", message);
        }
        notificationService.broadcastWebSocketNotification("MAINTENANCE_SCHEDULE", "Bakım Hatırlatıcısı", message, "warning");
        log.info("Maintenance schedule reminder sent for {} items", upcoming.size());
    }
}
