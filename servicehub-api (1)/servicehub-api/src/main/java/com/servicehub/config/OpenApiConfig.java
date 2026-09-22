package com.servicehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados globais exibidos no topo do Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceHub API")
                        .description("API REST para gerenciamento de serviços oferecidos por prestadores. "
                                + "Nesta etapa (Checkpoint) o recurso Serviços possui CRUD completo, "
                                + "validação de dados, paginação e tratamento padronizado de erros.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Projeto PBL ServiceHub API"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
