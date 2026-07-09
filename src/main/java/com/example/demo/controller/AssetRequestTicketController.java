package com.example.demo.controller;

import com.example.demo.entity.AssetRequestTicket;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetRequestTicketRepository;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-request-tickets")
@RequiredArgsConstructor
public class AssetRequestTicketController {

    private final AssetRequestTicketRepository ticketRepository;
    private final KullaniciRepository kullaniciRepository;
    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @GetMapping
    public ResponseEntity<List<AssetRequestTicket>> getAll(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(ticketRepository.findByStatusOrderByCreatedAtDesc(status));
        }
        return ResponseEntity.ok(ticketRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/my")
    public ResponseEntity<List<AssetRequestTicket>> getMy(Authentication auth) {
        Kullanici me = kullaniciRepository.findByEmail(auth.getName()).orElse(null);
        if (me == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(ticketRepository.findByRequesterIdOrderByCreatedAtDesc(me.getId()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        Kullanici me = kullaniciRepository.findByEmail(auth.getName()).orElse(null);
        if (me == null) return ResponseEntity.status(401).build();

        String description = (String) body.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Açıklama gerekli."));
        }

        AssetRequestTicket ticket = new AssetRequestTicket();
        ticket.setRequesterId(me.getId());
        ticket.setRequesterName(me.getFirstName() + " " + me.getLastName());
        ticket.setRequesterEmail(me.getEmail());
        ticket.setDescription(description);
        ticket.setStatus("PENDING");

        if (body.get("assetId") instanceof Number n) {
            ticket.setAssetId(n.longValue());
            ticket.setAssetName((String) body.get("assetName"));
        }
        if (body.containsKey("category")) {
            ticket.setCategory((String) body.get("category"));
        }

        AssetRequestTicket saved = ticketRepository.save(ticket);

        // Notify admins
        kullaniciRepository.findByRoleAndActiveTrue(Kullanici.Role.ADMIN).forEach(admin ->
            notificationService.create("info",
                me.getFirstName() + " " + me.getLastName() + " bir varlık talebinde bulundu.",
                admin.getEmail()));

        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        return ticketRepository.findById(id).map(t -> {
            t.setStatus("APPROVED");
            t.setResolvedBy(auth.getName());
            t.setResolvedAt(LocalDateTime.now());
            if (body != null) t.setResolverNote(body.get("note"));
            ticketRepository.save(t);

            notificationService.create("success",
                "Varlık talebiniz onaylandı: " + t.getDescription().substring(0, Math.min(60, t.getDescription().length())),
                t.getRequesterEmail());

            return ResponseEntity.ok(t);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}/deny")
    public ResponseEntity<?> deny(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        return ticketRepository.findById(id).map(t -> {
            t.setStatus("DENIED");
            t.setResolvedBy(auth.getName());
            t.setResolvedAt(LocalDateTime.now());
            if (body != null) t.setResolverNote(body.get("note"));
            ticketRepository.save(t);

            notificationService.create("error",
                "Varlık talebiniz reddedildi: " + t.getDescription().substring(0, Math.min(60, t.getDescription().length())),
                t.getRequesterEmail());

            return ResponseEntity.ok(t);
        }).orElse(ResponseEntity.notFound().build());
    }
}
