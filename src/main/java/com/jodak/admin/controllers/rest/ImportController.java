package com.jodak.admin.controllers.rest;

import com.jodak.admin.dtos.ImportErrorResponse;
import com.jodak.admin.dtos.ImportJobResponse;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportFormat;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.admin.enums.ImportStatus;
import com.jodak.admin.services.ImportJobService;
import com.jodak.config.OpenApiConfig;
import com.jodak.dtos.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Gestion des imports asynchrones (réservé à {@code ROLE_ADMIN}).
 */
@RestController
@RequestMapping("/api/admin/imports")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(name = "Administration · Imports", description = "Import asynchrone de données")
public class ImportController {

    private final ImportJobService importJobService;

    @PostMapping
    @Operation(summary = "Lancer un import (asynchrone)")
    public ResponseEntity<ImportJobResponse> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam ImportJobType jobType,
            @RequestParam(defaultValue = "CSV") ImportFormat format,
            @RequestParam(defaultValue = "DRY_RUN") ImportMode mode,
            @RequestParam(defaultValue = "SKIP") DuplicateStrategy duplicateStrategy,
            @AuthenticationPrincipal String adminId) {
        ImportJobResponse job = importJobService.create(file, jobType, format, mode, duplicateStrategy,
                Long.valueOf(adminId));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(job.id()).toUri();
        return ResponseEntity.created(location).body(job);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter l'état d'un import")
    public ImportJobResponse getById(@PathVariable Long id) {
        return importJobService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lister les imports (paginé, filtrable par statut)")
    public PageResponse<ImportJobResponse> list(
            @RequestParam(required = false) ImportStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return importJobService.search(status, pageable);
    }

    @GetMapping("/{id}/errors")
    @Operation(summary = "Consulter le rapport d'erreurs d'un import")
    public PageResponse<ImportErrorResponse> errors(
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable) {
        return importJobService.errors(id, pageable);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Demander l'annulation d'un import")
    public void cancel(@PathVariable Long id) {
        importJobService.cancel(id);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "Compenser (annuler) un import committé")
    public ImportJobResponse rollback(@PathVariable Long id) {
        return importJobService.rollback(id);
    }
}
