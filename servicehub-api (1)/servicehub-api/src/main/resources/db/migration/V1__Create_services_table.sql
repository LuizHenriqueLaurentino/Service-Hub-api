CREATE TABLE services (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(150)  NOT NULL,
    description VARCHAR(1000),
    price       NUMERIC(10,2) NOT NULL CHECK (price > 0),
    category    VARCHAR(50)   NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Titulo unico sem diferenciar maiusculas de minusculas
CREATE UNIQUE INDEX uk_services_title_lower ON services (LOWER(title));

CREATE INDEX idx_services_category ON services (category);
