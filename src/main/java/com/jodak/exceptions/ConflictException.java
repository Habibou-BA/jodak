package com.jodak.exceptions;

/**
 * Levée en cas de conflit métier (unicité, suppression restreinte, …) (→ HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
