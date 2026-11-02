package com.servicehub.service;

import com.servicehub.dto.AuthResponse;
import com.servicehub.dto.LoginRequest;
import com.servicehub.dto.RegisterRequest;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.ConflictException;
import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.repository.UserRepository;
import com.servicehub.security.JwtService;
import com.servicehub.util.InputSanitizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Cadastro e login. A senha é verificada pelo AuthenticationManager (BCrypt) e,
 * se correta, o JwtService emite o token.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository users,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        AppUser user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        return toResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Não é permitido criar uma conta com o papel ADMIN.");
        }
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Já existe um usuário com o e-mail: " + email);
        }
        String name = InputSanitizer.sanitize(request.name());
        if (name == null || name.length() < 2) {
            throw new BadRequestException("O nome deve ter ao menos 2 caracteres após a remoção de HTML/scripts.");
        }
        AppUser saved = users.save(new AppUser(name, email,
                passwordEncoder.encode(request.password()), request.role()));
        return toResponse(saved);
    }

    private AuthResponse toResponse(AppUser user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                user.getEmail(), user.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
