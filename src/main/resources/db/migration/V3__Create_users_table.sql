CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'PROVIDER', 'CLIENT')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- E-mail unico sem diferenciar maiusculas de minusculas
CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));
