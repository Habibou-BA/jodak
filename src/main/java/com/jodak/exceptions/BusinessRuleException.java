package com.jodak.exceptions;

/**
 * Levée lorsqu'une règle métier n'est pas satisfaite alors que la requête est syntaxiquement
 * valide (→ HTTP 422).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
