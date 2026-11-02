package com.servicehub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato padrão de erro devolvido por toda a API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Formato padrão de erro da API")
public record ErrorResponse(

        @Schema(description = "Momento em que o erro ocorreu", example = "2026-09-21T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código de status HTTP", example = "404")
        int status,

        @Schema(description = "Descrição curta do status HTTP", example = "Not Found")
        String error,

        @Schema(description = "Mensagem explicativa do erro", example = "Serviço não encontrado: 42")
        String message,

        @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/v1/services/42")
        String path,

        @Schema(description = "Detalhes por campo (presente apenas em erros de validação)")
        List<FieldErrorDetail> details
) {

    @Schema(name = "FieldErrorDetail", description = "Erro de validação em um campo específico")
    public record FieldErrorDetail(

            @Schema(description = "Nome do campo inválido", example = "price")
            String field,

            @Schema(description = "Motivo da rejeição", example = "O preço deve ser maior que zero")
            String message
    ) {
    }
}
