package com.servicehub.model;

/**
 * Categorias de serviço oferecidas na plataforma.
 * Persistida como texto (EnumType.STRING), resiliente a reordenações do enum.
 */
public enum ServiceCategory {
    CLEANING,
    PLUMBING,
    ELECTRICAL,
    PAINTING,
    GARDENING,
    TECHNOLOGY,
    EDUCATION,
    OTHER
}
