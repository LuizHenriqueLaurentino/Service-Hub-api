package com.servicehub.model;

/**
 * Papéis de acesso da plataforma ServiceHub.
 * <ul>
 *   <li>ADMIN: administra tudo, inclusive remoção de serviços;</li>
 *   <li>PROVIDER: prestador, cria e edita serviços;</li>
 *   <li>CLIENT: cliente, apenas consulta.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    PROVIDER,
    CLIENT
}
