package com.jodak.exceptions;

/**
 * Levée lorsqu'une ressource demandée n'existe pas (→ HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
