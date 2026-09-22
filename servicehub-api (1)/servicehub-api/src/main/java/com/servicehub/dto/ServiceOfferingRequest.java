package com.servicehub.dto;

import com.servicehub.model.ServiceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "ServiceOfferingRequest",
        description = "Dados para criar (POST) ou substituir por completo (PUT) um serviço")
public record ServiceOfferingRequest(

        @Schema(description = "Título do serviço. Deve ser único, sem diferenciar maiúsculas de minúsculas",
                example = "Instalação de ventilador de teto", minLength = 3, maxLength = 150)
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 150, message = "O título deve ter entre 3 e 150 caracteres")
        String title,

        @Schema(description = "Descrição detalhada do que está incluso no serviço",
                example = "Instalação completa com fixação no teto, ligação elétrica e teste de funcionamento.",
                maxLength = 1000)
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        @Schema(description = "Preço do serviço em reais (BRL)", example = "150.00", minimum = "0.01")
        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço aceita até 8 dígitos inteiros e 2 casas decimais")
        BigDecimal price,

        @Schema(description = "Categoria do serviço", example = "ELECTRICAL")
        @NotNull(message = "A categoria é obrigatória")
        ServiceCategory category,

        @Schema(description = "Indica se o serviço está disponível. Quando omitido, assume true",
                example = "true")
        Boolean active
) {
}
