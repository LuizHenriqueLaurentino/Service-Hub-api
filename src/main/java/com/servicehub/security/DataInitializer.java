package com.servicehub.security;

import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um usuário de cada papel na primeira execução, para facilitar a demonstração.
 * A senha é gravada como hash BCrypt. Desative com app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String password;

    public DataInitializer(UserRepository users, PasswordEncoder encoder,
                           @Value("${app.seed.password:Senha@123}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        seed("Administrador", "admin@servicehub.com", Role.ADMIN);
        seed("Prestador Demo", "provider@servicehub.com", Role.PROVIDER);
        seed("Cliente Demo", "client@servicehub.com", Role.CLIENT);
    }

    private void seed(String name, String email, Role role) {
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new AppUser(name, email, encoder.encode(password), role));
            log.info("Usuário de demonstração criado: {} ({})", email, role);
        }
    }
}
