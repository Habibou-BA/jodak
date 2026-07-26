package com.jodak.admin.exceptions;

/**
 * Fichier ou paramètres d'import invalides (→ HTTP 400).
 */
public class ImportValidationException extends RuntimeException {

    public ImportValidationException(String message) {
        super(message);
    }
}
