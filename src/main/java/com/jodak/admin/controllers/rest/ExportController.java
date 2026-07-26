package com.jodak.admin.controllers.rest;

import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.maintenance.DataExportService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Export de toutes les données (archive ZIP + metadata). Réservé à {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Administration · Export", description = "Export des données (ZIP)")
public class ExportController {

    private final DataExportService dataExportService;
    private final CurrentAdminService currentAdminService;
    private final AdminLogService adminLogService;

    @GetMapping
    @Operation(summary = "Exporter toutes les données au format ZIP")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal String adminId,
                                         HttpServletRequest request) {
        AdminUser admin = currentAdminService.require(adminId);
        DataExportService.ArchiveResult archive = dataExportService.build(admin.getEmail());
        adminLogService.record(admin.getId(), "EXPORT", true, null,
                clientIp(request), request.getHeader("User-Agent"));
        String fileName = "export-" + LocalDate.now() + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(archive.content());
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
