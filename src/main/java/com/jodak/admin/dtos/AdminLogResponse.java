package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Entrée du journal d'audit d'administration.
 */
@Schema(description = "Entrée du journal d'administration")
public record AdminLogResponse(
        Long id,
        Long adminId,
        String action,
        boolean success,
        String message,
        String ip,
        OffsetDateTime createdAt
) {
}
