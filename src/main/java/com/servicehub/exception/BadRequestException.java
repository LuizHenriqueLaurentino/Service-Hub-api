package com.servicehub.exception;

/**
 * Lançada quando a requisição é sintaticamente válida, mas viola uma regra de entrada
 * (por exemplo, título vazio após a sanitização). Vira HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
