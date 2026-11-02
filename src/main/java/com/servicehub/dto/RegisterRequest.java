package com.servicehub.dto;

import com.servicehub.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Dados para criar uma conta. O papel ADMIN não pode ser auto-atribuído")
public record RegisterRequest(

        @Schema(description = "Nome completo", example = "Maria Prestadora")
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String name,

        @Schema(description = "E-mail (único)", example = "maria@exemplo.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @Schema(description = "Senha (8 a 72 caracteres, limite do BCrypt)", example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password,

        @Schema(description = "Papel desejado: PROVIDER ou CLIENT", example = "CLIENT")
        @NotNull(message = "O papel é obrigatório")
        Role role
) {
}
