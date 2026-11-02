package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credenciais para obter um token JWT")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado", example = "admin@servicehub.com")
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
