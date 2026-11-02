package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(name = "PageResponse", description = "Página de resultados com metadados de paginação")
public record PageResponse<T>(

        @Schema(description = "Itens da página atual")
        List<T> content,

        @Schema(description = "Número da página atual (começa em 0)", example = "0")
        int page,

        @Schema(description = "Quantidade máxima de itens por página", example = "10")
        int size,

        @Schema(description = "Total de itens encontrados", example = "8")
        long totalElements,

        @Schema(description = "Total de páginas", example = "1")
        int totalPages
) {

    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
