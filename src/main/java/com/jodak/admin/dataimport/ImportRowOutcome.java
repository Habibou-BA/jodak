package com.jodak.admin.dataimport;

/**
 * Résultat de l'import d'une ligne. En cas d'échec, porte le détail d'erreur ; en cas de
 * création/mise à jour, porte la référence de l'entité (pour la compensation).
 */
public record ImportRowOutcome(
        Result result,
        String column,
        String errorCode,
        String message,
        String entityType,
        Long entityId
) {

    public enum Result { IMPORTED, UPDATED, SKIPPED, FAILED }

    public static ImportRowOutcome imported(String entityType, Long entityId) {
        return new ImportRowOutcome(Result.IMPORTED, null, null, null, entityType, entityId);
    }

    public static ImportRowOutcome updated(String entityType, Long entityId) {
        return new ImportRowOutcome(Result.UPDATED, null, null, null, entityType, entityId);
    }

    public static ImportRowOutcome skipped(String message) {
        return new ImportRowOutcome(Result.SKIPPED, null, null, message, null, null);
    }

    public static ImportRowOutcome failed(String column, String errorCode, String message) {
        return new ImportRowOutcome(Result.FAILED, column, errorCode, message, null, null);
    }
}
