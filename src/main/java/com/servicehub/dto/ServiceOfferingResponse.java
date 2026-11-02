package com.servicehub.dto;

import com.servicehub.model.ServiceCategory;
import com.servicehub.model.ServiceOffering;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "ServiceOfferingResponse", description = "Representação de um serviço retornada pela API")
public record ServiceOfferingResponse(

        @Schema(description = "Identificador único do serviço", example = "1")
        Long id,

        @Schema(description = "Título do serviço", example = "Instalação de chuveiro elétrico")
        String title,

        @Schema(description = "Descrição do serviço",
                example = "Instalação completa com ligação elétrica, fixação e teste de funcionamento.")
        String description,

        @Schema(description = "Preço em reais (BRL)", example = "180.00")
        BigDecimal price,

        @Schema(description = "Categoria do serviço", example = "ELECTRICAL")
        ServiceCategory category,

        @Schema(description = "Indica se o serviço está disponível", example = "true")
        boolean active,

        @Schema(description = "Data e hora de criação", example = "2026-09-21T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Data e hora da última atualização", example = "2026-09-21T10:30:00")
        LocalDateTime updatedAt
) {

    public static ServiceOfferingResponse fromEntity(ServiceOffering entity) {
        return new ServiceOfferingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
