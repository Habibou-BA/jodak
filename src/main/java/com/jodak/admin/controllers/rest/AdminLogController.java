package com.jodak.admin.controllers.rest;

import com.jodak.admin.dtos.AdminLogResponse;
import com.jodak.admin.services.AdminLogService;
import com.jodak.config.OpenApiConfig;
import com.jodak.dtos.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation du journal d'audit d'administration. Réservé à {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Administration · Journal", description = "Journal des opérations d'administration")
public class AdminLogController {

    private final AdminLogService adminLogService;

    @GetMapping
    @Operation(summary = "Lister les entrées du journal (paginé, plus récentes d'abord)")
    public PageResponse<AdminLogResponse> list(@PageableDefault(size = 50) Pageable pageable) {
        return adminLogService.search(pageable);
    }
}
