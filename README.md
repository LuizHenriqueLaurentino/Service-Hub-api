# ServiceHub API

API REST para gerenciamento de serviços oferecidos por prestadores, desenvolvida com Java e Spring Boot
no projeto PBL da disciplina de Desenvolvimento Backend. Esta versão corresponde à **Fase 2**: segurança (Aula 8), JWT, papéis e GraphQL (Aula 9) e testes automatizados (Aula 10).

## O que já está implementado

| Requisito do Checkpoint | Situação |
|---|---|
| Projeto Spring Boot (Spring Web, Spring Data JPA, PostgreSQL, SpringDoc OpenAPI) | Pronto |
| Recurso com CRUD completo (Serviços): POST, GET, PUT, PATCH e DELETE | Pronto |
| Swagger UI em `/swagger-ui.html` com summaries, descriptions, schemas e exemplos | Pronto |
| Tratamento de erros padronizado (400, 404, 409, 500) | Pronto |
| Separação Controller, Service e Repository | Pronto |

Extras: paginação com filtros, migrations com Flyway e validação com Bean Validation.

### Fase 2

| Entrega | Situação | Onde está |
|---|---|---|
| Aula 8: Spring Security com `SecurityFilterChain` | Pronto | `security/SecurityConfig` |
| Aula 8: senhas com BCrypt | Pronto | `BCryptPasswordEncoder` em `SecurityConfig`; `AuthService`, `DataInitializer` |
| Aula 8: Bean Validation + tratamento global de erros (400, 401, 403, 404, 409, 500) | Pronto | DTOs `*Request`, `exception/GlobalExceptionHandler` |
| Aula 8: sanitização de entradas (XSS) | Pronto | `util/InputSanitizer`, usado em `ServiceOfferingService` e `AuthService` |
| Aula 8: proteção de rotas com `@PreAuthorize` | Pronto | `ServiceOfferingController`, `ServiceGraphQLController` |
| Aula 9: login com JWT e filtro de autenticação | Pronto | `AuthController`, `AuthService`, `JwtService`, `JwtAuthFilter` |
| Aula 9: papéis `ADMIN`, `PROVIDER` e `CLIENT` | Pronto | `model/Role`, `model/AppUser`, migration `V3` |
| Aula 9: endpoint alternativo GraphQL | Pronto | `graphql/ServiceGraphQLController`, `resources/graphql/schema.graphqls` |
| Aula 10: testes unitários do service com Mockito | Pronto | `ServiceOfferingServiceTest`, `InputSanitizerTest`, `JwtServiceTest` |
| Aula 10: testes do controller com MockMvc | Pronto | `ServiceOfferingControllerTest` |
| Aula 10: teste de integração (H2 em memória) | Pronto | `integration/ServiceHubIntegrationTest` |

## Tecnologias

- Java 17 (funciona também em JDK 21)
- Spring Boot 3.3.5 (Web, Data JPA, Validation, Security, GraphQL)
- JWT com jjwt 0.12.6 (HS256) e senhas com BCrypt
- PostgreSQL 15 (via Docker Compose)
- Flyway (versionamento do schema)
- SpringDoc OpenAPI 2.6.0 (Swagger UI)
- Maven, JUnit 5, Mockito, MockMvc e H2 (apenas nos testes)

## Como executar

Pré-requisitos: JDK 17 ou superior, Maven e Docker.

```bash
# 1. Subir o PostgreSQL
docker compose up -d

# 2. Executar a aplicação
mvn spring-boot:run
```

Se preferir, abra o projeto no IntelliJ IDEA, aguarde o Maven importar as dependências e execute a classe
`ServicehubApiApplication`.

Ao iniciar, o Flyway cria a tabela `services` e insere alguns dados de exemplo.

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| API | http://localhost:8080/api/v1/services |

A conexão com o banco usa por padrão o usuário `servicehub`, senha `secret` e banco `servicehub`
(os mesmos valores do `docker-compose.yml`). Para usar outro banco, defina as variáveis de ambiente
`DB_URL`, `DB_USER` e `DB_PASSWORD`.

Para rodar os testes: `mvn test` (usam H2 em memória; não precisam de PostgreSQL nem Docker).

## Autenticação (JWT) e papéis

Na primeira execução são criados 3 usuários de demonstração (senha padrão `Senha@123`, configurável com `SEED_PASSWORD`;
desative com `app.seed.enabled=false`):

| E-mail | Papel |
|---|---|
| `admin@servicehub.com` | ADMIN |
| `provider@servicehub.com` | PROVIDER |
| `client@servicehub.com` | CLIENT |

```bash
# 1. Login (rota pública) -> devolve o token
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@servicehub.com","password":"Senha@123"}'

# 2. Usar o token nas demais rotas
curl http://localhost:8080/api/v1/services -H "Authorization: Bearer <token>"
```

No Swagger UI, faça o login em `POST /api/v1/auth/login`, clique em **Authorize** e cole o token.
O segredo do JWT vem de `JWT_SECRET` (mínimo 32 caracteres) e a validade padrão é de 24 horas (`JWT_EXPIRATION_MS`).

| Rota | ADMIN | PROVIDER | CLIENT | Sem token |
|---|---|---|---|---|
| `POST /api/v1/auth/login`, `POST /api/v1/auth/register` | Sim | Sim | Sim | Sim (públicas) |
| `GET /api/v1/services`, `GET /api/v1/services/{id}`, `POST /graphql` | Sim | Sim | Sim | 401 |
| `POST`, `PUT` e `PATCH /api/v1/services` | Sim | Sim | 403 | 401 |
| `DELETE /api/v1/services/{id}` | Sim | 403 | 403 | 401 |

`POST /api/v1/auth/register` cria contas `PROVIDER` ou `CLIENT`; pedir `ADMIN` retorna 400.

## GraphQL (endpoint alternativo)

`POST http://localhost:8080/graphql` (mesmo token JWT). Interface GraphiQL em http://localhost:8080/graphiql (envie o header `Authorization`).

```graphql
{
  services(category: PLUMBING, active: true, page: 0, size: 5) {
    totalElements
    content { id title price category }
  }
  serviceById(id: 1) { title description price }
}
```

## Endpoints

Base: `/api/v1/services`

| Método | URI | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/v1/services` | Cria um serviço | 201 (com header Location) | 400, 409 |
| GET | `/api/v1/services` | Lista serviços com paginação e filtros | 200 | 400 |
| GET | `/api/v1/services/{id}` | Busca um serviço por ID | 200 | 400, 404 |
| PUT | `/api/v1/services/{id}` | Substitui o serviço por completo | 200 | 400, 404, 409 |
| PATCH | `/api/v1/services/{id}` | Atualiza campos específicos | 200 | 400, 404, 409 |
| DELETE | `/api/v1/services/{id}` | Remove o serviço | 204 | 400, 404 |

Parâmetros de `GET /api/v1/services`: `category`, `active`, `page` (padrão 0) e `size` (padrão 10, máximo 100).

Categorias válidas: `CLEANING`, `PLUMBING`, `ELECTRICAL`, `PAINTING`, `GARDENING`, `TECHNOLOGY`, `EDUCATION`, `OTHER`.

## Exemplos com curl

```bash
# Criar
curl -i -X POST http://localhost:8080/api/v1/services \
  -H "Content-Type: application/json" \
  -d '{"title":"Instalação de ventilador de teto","description":"Fixação, ligação elétrica e teste.","price":150.00,"category":"ELECTRICAL","active":true}'

# Listar (filtrando por categoria)
curl "http://localhost:8080/api/v1/services?category=PLUMBING&page=0&size=5"

# Buscar por ID
curl http://localhost:8080/api/v1/services/1

# Substituir (PUT)
curl -X PUT http://localhost:8080/api/v1/services/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Instalação de chuveiro elétrico","description":"Versão atualizada.","price":190.00,"category":"ELECTRICAL","active":true}'

# Atualizar só o preço (PATCH)
curl -X PATCH http://localhost:8080/api/v1/services/1 \
  -H "Content-Type: application/json" \
  -d '{"price":175.50}'

# Remover
curl -i -X DELETE http://localhost:8080/api/v1/services/1
```

### Formato padrão de erro

```json
{
  "timestamp": "2026-09-21T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Falha na validação dos dados enviados.",
  "path": "/api/v1/services",
  "details": [
    { "field": "price", "message": "O preço deve ser maior que zero" }
  ]
}
```

O campo `details` só aparece em erros de validação.

## Arquitetura

```
src/main/java/com/servicehub
├── ServicehubApiApplication.java
├── config/       OpenApiConfig (metadados do Swagger e esquema Bearer)
├── controller/   ServiceOfferingController e AuthController (HTTP + OpenAPI, sem regra de negócio)
├── graphql/      ServiceGraphQLController e GraphQLExceptionResolver
├── security/     SecurityConfig, JwtService, JwtAuthFilter, CustomUserDetailsService, DataInitializer,
│                 RestAuthenticationEntryPoint (401) e RestAccessDeniedHandler (403)
├── service/      ServiceOfferingService e AuthService (regras de negócio e transações)
├── repository/   ServiceOfferingRepository e UserRepository (Spring Data JPA)
├── model/        ServiceOffering, AppUser (entidades), ServiceCategory e Role (enums)
├── dto/          Request, PatchRequest, Response, PageResponse, ErrorResponse, LoginRequest, RegisterRequest, AuthResponse
├── util/         InputSanitizer (remoção de HTML/scripts)
└── exception/    Exceções de negócio e GlobalExceptionHandler (@RestControllerAdvice)

src/main/resources
├── application.properties
├── graphql/schema.graphqls
└── db/migration  V1 (tabela services), V2 (dados de exemplo) e V3 (tabela users)

src/test
├── java/.../service/      ServiceOfferingServiceTest (Mockito)
├── java/.../util/         InputSanitizerTest
├── java/.../security/     JwtServiceTest
├── java/.../controller/   ServiceOfferingControllerTest (MockMvc)
├── java/.../integration/  ServiceHubIntegrationTest (SpringBootTest + H2)
└── resources/application.properties  (H2 em memória, Flyway desligado)
```

Fluxo de uma requisição: o `JwtAuthFilter` valida o token e preenche o papel do usuário, `@PreAuthorize` autoriza,
o `Controller` recebe e valida o JSON, `Service` aplica as regras e chama o
`Repository`, que conversa com o PostgreSQL. A entidade nunca é exposta diretamente: a API responde com DTOs.

### Regras de negócio (camada Service)

- O título do serviço é único, sem diferenciar maiúsculas de minúsculas (409 Conflict se já existir).
- Título e descrição são sanitizados: tags HTML e scripts são removidos e os espaços extras do início e do fim são cortados. Título com menos de 3 caracteres após a limpeza retorna 400.
- Quando `active` é omitido na criação, o serviço nasce ativo.
- Paginação normalizada: página mínima 0, tamanho entre 1 e 100.

## Próximos passos

- Otimizar consultas com índices adicionais, depois de medir as consultas mais usadas
- Finalizar a execução da API e do PostgreSQL em containers (Dockerfile da API, volumes persistentes e healthcheck no `docker-compose.yml`)
- Recursos de contratos e avaliações, com relacionamentos JPA
