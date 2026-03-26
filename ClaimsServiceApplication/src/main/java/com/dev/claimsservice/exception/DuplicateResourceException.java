package com.dev.claimsservice.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String entity, String field, Object value) {
        super(String.format("%s already exists with %s: %s", entity, field, value));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
