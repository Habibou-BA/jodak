package com.jodak.admin.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Une ligne rejetée du rapport d'import.
 */
@Schema(description = "Erreur de ligne d'import")
public record ImportErrorResponse(
        long rowNumber,
        String columnName,
        String rawValue,
        String errorCode,
        String message
) {
}
