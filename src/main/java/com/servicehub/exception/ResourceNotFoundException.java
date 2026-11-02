package com.servicehub.exception;

/**
 * Lançada quando um recurso solicitado não existe. Vira HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
