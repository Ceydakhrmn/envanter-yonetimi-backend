package com.example.demo.service;

import com.example.demo.entity.Asset;
import com.example.demo.entity.Kullanici;
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
        }
        notificationService.broadcastWebSocketNotification("ASSET_WARRANTY", "Garanti Hatırlatıcısı", message, "warning");
        log.info("Warranty reminder sent for {} assets", assets.size());
    }
}
