package com.jodak.admin.exceptions;

/**
 * Identifiants invalides (message générique, sans divulgation) (→ HTTP 401).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
