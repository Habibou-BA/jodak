package com.jodak.admin.controllers.rest;

import com.jodak.admin.dtos.ResetRequest;
import com.jodak.admin.dtos.ResetResponse;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.maintenance.ResetService;
import com.jodak.admin.security.CurrentAdminService;
import com.jodak.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Réinitialisation sécurisée de la plateforme (double confirmation). Réservé à {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/admin/reset")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Administration · Réinitialisation", description = "Réinitialisation sécurisée")
public class ResetController {

    private final ResetService resetService;
    private final CurrentAdminService currentAdminService;

    @PostMapping
    @Operation(summary = "Réinitialiser la plateforme (sauvegarde préalable + double confirmation)")
    public ResetResponse reset(@Valid @RequestBody ResetRequest request,
                               @AuthenticationPrincipal String adminId,
                               HttpServletRequest http) {
        AdminUser admin = currentAdminService.require(adminId);
        return resetService.reset(admin.getId(), request.password(), request.confirmationPhrase(),
                clientIp(http), http.getHeader("User-Agent"));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
