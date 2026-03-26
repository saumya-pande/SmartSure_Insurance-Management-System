package com.dev.authentication.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, String field, Object value) {
        super(String.format("%s not found with %s: %s", entity, field, value));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
