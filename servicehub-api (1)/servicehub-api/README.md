# ServiceHub API

API REST para gerenciamento de serviços oferecidos por prestadores, desenvolvida com Java e Spring Boot
no projeto PBL da disciplina de Desenvolvimento Backend. Esta entrega corresponde ao **Checkpoint**.

## O que já está implementado

| Requisito do Checkpoint | Situação |
|---|---|
| Projeto Spring Boot (Spring Web, Spring Data JPA, PostgreSQL, SpringDoc OpenAPI) | Pronto |
| Recurso com CRUD completo (Serviços): POST, GET, PUT, PATCH e DELETE | Pronto |
| Swagger UI em `/swagger-ui.html` com summaries, descriptions, schemas e exemplos | Pronto |
| Tratamento de erros padronizado (400, 404, 409, 500) | Pronto |
| Separação Controller, Service e Repository | Pronto |

Extras: paginação com filtros, migrations com Flyway, validação com Bean Validation e testes unitários da camada de serviço.

## Tecnologias

- Java 17 (funciona também em JDK 21)
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- PostgreSQL 15 (via Docker Compose)
- Flyway (versionamento do schema)
- SpringDoc OpenAPI 2.6.0 (Swagger UI)
- Maven, JUnit 5 e Mockito

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

Para rodar os testes: `mvn test` (não precisam de banco de dados).

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
├── config/       OpenApiConfig (metadados do Swagger)
├── controller/   ServiceOfferingController (HTTP + anotações OpenAPI, sem regra de negócio)
├── service/      ServiceOfferingService (regras de negócio e transações)
├── repository/   ServiceOfferingRepository (Spring Data JPA)
├── model/        ServiceOffering (entidade) e ServiceCategory (enum)
├── dto/          Request, PatchRequest, Response, PageResponse e ErrorResponse
└── exception/    Exceções de negócio e GlobalExceptionHandler (@RestControllerAdvice)

src/main/resources
├── application.properties
└── db/migration  V1 (tabela services) e V2 (dados de exemplo)
```

Fluxo de uma requisição: `Controller` recebe e valida o JSON, `Service` aplica as regras e chama o
`Repository`, que conversa com o PostgreSQL. A entidade nunca é exposta diretamente: a API responde com DTOs.

### Regras de negócio (camada Service)

- O título do serviço é único, sem diferenciar maiúsculas de minúsculas (409 Conflict se já existir).
- Espaços extras no início e no fim de título e descrição são removidos.
- Quando `active` é omitido na criação, o serviço nasce ativo.
- Paginação normalizada: página mínima 0, tamanho entre 1 e 100.

## Próximos passos (Sprints seguintes)

- Recursos de usuários/prestadores, contratos e avaliações, com relacionamentos JPA
- Autenticação e autorização com Spring Security e JWT
- Testes de integração e documentação da autenticação no Swagger
