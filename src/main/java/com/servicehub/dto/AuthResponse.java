package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Token JWT emitido após login ou cadastro")
public record AuthResponse(

        @Schema(description = "Token JWT (enviar em 'Authorization: Bearer <token>')")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Validade do token em segundos", example = "86400")
        long expiresInSeconds,

        @Schema(description = "E-mail do usuário autenticado", example = "admin@servicehub.com")
        String email,

        @Schema(description = "Papel do usuário", example = "ADMIN")
        String role
) {
}
