package com.servicehub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                                + "Possui CRUD de serviços, validação, paginação, tratamento padronizado de erros, "
                                + "autenticação JWT com papéis ADMIN, PROVIDER e CLIENT e endpoint GraphQL em /graphql. "
                                + "Faça login em /api/v1/auth/login e use o botão Authorize com o token.")
                        .version("v2.0.0")
                        .contact(new Contact().name("Projeto PBL ServiceHub API"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
