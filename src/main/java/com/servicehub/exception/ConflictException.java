package com.servicehub.exception;

/**
 * Lançada quando a operação viola uma regra de negócio de unicidade. Vira HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
