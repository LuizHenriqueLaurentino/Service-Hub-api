package com.servicehub.security;

import com.servicehub.model.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "chave-de-teste-com-mais-de-32-caracteres-123456";

    @Test
    @DisplayName("generateToken: o token contém e-mail (sub) e papel (role) e é válido")
    void generatesValidTokenWithClaims() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        String token = jwt.generateToken("admin@servicehub.com", Role.ADMIN);

        Optional<Claims> claims = jwt.parse(token);
        assertTrue(claims.isPresent());
        assertEquals("admin@servicehub.com", claims.get().getSubject());
        assertEquals("ADMIN", claims.get().get("role", String.class));
        assertTrue(jwt.isValid(token));
    }

    @Test
    @DisplayName("parse: token expirado é rejeitado")
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -1_000);

        String token = jwt.generateToken("client@servicehub.com", Role.CLIENT);

        assertFalse(jwt.isValid(token));
    }

    @Test
    @DisplayName("parse: token adulterado ou assinado com outra chave é rejeitado")
    void tamperedOrForeignTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        JwtService other = new JwtService("outra-chave-totalmente-diferente-com-32-chars-xyz", 60_000);

        String token = jwt.generateToken("provider@servicehub.com", Role.PROVIDER);
        String tampered = token.substring(0, token.length() - 3) + "abc";
        String foreign = other.generateToken("provider@servicehub.com", Role.PROVIDER);

        assertFalse(jwt.isValid(tampered));
        assertFalse(jwt.isValid(foreign));
        assertFalse(jwt.isValid("isso-nao-e-um-jwt"));
    }

    @Test
    @DisplayName("construtor: segredo com menos de 32 caracteres lança IllegalStateException")
    void shortSecretIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("curto", 1000));
    }

    @Test
    @DisplayName("getExpirationSeconds: converte milissegundos em segundos")
    void expirationInSeconds() {
        assertEquals(86400, new JwtService(SECRET, 86_400_000).getExpirationSeconds());
    }
}
