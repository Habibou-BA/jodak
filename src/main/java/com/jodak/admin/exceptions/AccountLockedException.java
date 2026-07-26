package com.jodak.admin.exceptions;

/**
 * Compte temporairement verrouillé après trop de tentatives (→ HTTP 423).
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }
}
