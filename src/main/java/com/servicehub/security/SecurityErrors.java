package com.servicehub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

/** Escreve respostas 401/403 no mesmo formato ErrorResponse do restante da API. */
final class SecurityErrors {

    private SecurityErrors() {
    }

    static void write(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), null);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
