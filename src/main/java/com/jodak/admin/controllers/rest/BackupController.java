package com.jodak.admin.controllers.rest;

import com.jodak.admin.dtos.BackupResponse;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.maintenance.BackupService;
import com.jodak.admin.security.CurrentAdminService;
import com.jodak.admin.services.AdminLogService;
import com.jodak.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sauvegarde logique des données (ZIP stocké côté serveur). Réservé à {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/admin/backup")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Administration · Sauvegarde", description = "Sauvegarde et restauration logiques")
public class BackupController {

    private final BackupService backupService;
    private final CurrentAdminService currentAdminService;
    private final AdminLogService adminLogService;

    @PostMapping
    @Operation(summary = "Créer une sauvegarde complète")
    public BackupResponse create(@AuthenticationPrincipal String adminId, HttpServletRequest request) {
        AdminUser admin = currentAdminService.require(adminId);
        BackupResponse backup = backupService.create(admin.getEmail());
        adminLogService.record(admin.getId(), "BACKUP", true, backup.fileName(),
                clientIp(request), request.getHeader("User-Agent"));
        return backup;
    }

    @GetMapping("/{fileName}/download")
    @Operation(summary = "Télécharger une sauvegarde existante")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        Path file = backupService.resolveExisting(fileName);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(Files.readAllBytes(file));
        } catch (IOException ex) {
            throw new IllegalStateException("Échec de la lecture de la sauvegarde.", ex);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
