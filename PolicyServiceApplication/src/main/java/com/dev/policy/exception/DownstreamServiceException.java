package com.dev.policy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DownstreamServiceException extends RuntimeException {
    private final HttpStatus status;
    private final String error;
    private final String message;

    public DownstreamServiceException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
