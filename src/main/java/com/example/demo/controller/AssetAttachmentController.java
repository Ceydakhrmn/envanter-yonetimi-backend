package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetAttachment;
import com.example.demo.repository.AssetAttachmentRepository;
import com.example.demo.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetAttachmentController {

    private final AssetAttachmentRepository attachmentRepository;
    private final AssetRepository assetRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain", "text/csv"
    );

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{assetId}/attachments")
    public ResponseEntity<?> upload(
            @PathVariable Long assetId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dosya boş olamaz"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dosya boyutu 10MB'ı aşamaz"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bu dosya türü desteklenmiyor"));
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Varlık bulunamadı"));

        AssetAttachment attachment = new AssetAttachment();
        attachment.setAsset(asset);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setData(file.getBytes());
        attachment.setUploadedBy(authentication.getName());

        AssetAttachment saved = attachmentRepository.save(attachment);

        return ResponseEntity.ok(Map.of(
            "id", saved.getId(),
            "fileName", saved.getFileName(),
            "contentType", saved.getContentType(),
            "fileSize", saved.getFileSize(),
            "uploadedBy", saved.getUploadedBy(),
            "createdAt", saved.getCreatedAt().toString()
        ));
    }

    @GetMapping("/{assetId}/attachments")
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable Long assetId) {
        List<AssetAttachment> attachments = attachmentRepository.findByAssetIdOrderByCreatedAtDesc(assetId);
        List<Map<String, Object>> result = attachments.stream().map(a -> Map.<String, Object>of(
            "id", a.getId(),
            "fileName", a.getFileName(),
            "contentType", a.getContentType(),
            "fileSize", a.getFileSize(),
            "uploadedBy", a.getUploadedBy() != null ? a.getUploadedBy() : "",
            "createdAt", a.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long attachmentId) {
        AssetAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Dosya bulunamadı"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getData());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId) {
        if (!attachmentRepository.existsById(attachmentId)) {
            return ResponseEntity.notFound().build();
        }
        attachmentRepository.deleteById(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
