package com.servicehub.dto;

import com.servicehub.model.ServiceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "ServiceOfferingPatchRequest",
        description = "Atualização parcial (PATCH) de um serviço. Apenas os campos enviados são alterados")
public record ServiceOfferingPatchRequest(

        @Schema(description = "Novo título do serviço", example = "Instalação de ventilador de teto com luminária",
                minLength = 3, maxLength = 150)
        @Size(min = 3, max = 150, message = "O título deve ter entre 3 e 150 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "O título não pode ser vazio")
        String title,

        @Schema(description = "Nova descrição do serviço", example = "Inclui a montagem da luminária.",
                maxLength = 1000)
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        @Schema(description = "Novo preço em reais (BRL)", example = "175.50", minimum = "0.01")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço aceita até 8 dígitos inteiros e 2 casas decimais")
        BigDecimal price,

        @Schema(description = "Nova categoria do serviço", example = "ELECTRICAL")
        ServiceCategory category,

        @Schema(description = "Ativa ou desativa o serviço", example = "false")
        Boolean active
) {
}
