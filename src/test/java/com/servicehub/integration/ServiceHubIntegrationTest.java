package com.servicehub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.model.ServiceCategory;
import com.servicehub.model.ServiceOffering;
import com.servicehub.repository.ServiceOfferingRepository;
import com.servicehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração: sobe a aplicação inteira (Security + JWT + JPA + GraphQL) com banco H2
 * em memória e percorre o fluxo real: login, token, rotas protegidas por papel e GraphQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ServiceHubIntegrationTest {

    private static final String PASSWORD = "Senha@123";
    private static final String SERVICES = "/api/v1/services";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private ServiceOfferingRepository services;
    @Autowired
    private PasswordEncoder encoder;

    // Instanciado diretamente em vez de injetado
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        services.deleteAll();
        users.deleteAll();
        users.save(new AppUser("Admin", "admin@teste.com", encoder.encode(PASSWORD), Role.ADMIN));
        users.save(new AppUser("Prestador", "provider@teste.com", encoder.encode(PASSWORD), Role.PROVIDER));
        users.save(new AppUser("Cliente", "client@teste.com", encoder.encode(PASSWORD), Role.CLIENT));
    }

    // ---------- Auxiliares ----------

    private String login(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", PASSWORD));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String bearer(String email) throws Exception {
        return "Bearer " + login(email);
    }

    private String serviceJson(String title) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title, "description", "Descrição do serviço",
                "price", 150.00, "category", "ELECTRICAL", "active", true));
    }

    private ServiceOffering saveService(String title) {
        ServiceOffering entity = new ServiceOffering();
        entity.setTitle(title);
        entity.setDescription("Descrição");
        entity.setPrice(new BigDecimal("99.90"));
        entity.setCategory(ServiceCategory.CLEANING);
        entity.setActive(true);
        return services.save(entity);
    }

    /** O endpoint GraphQL responde de forma assíncrona: se o MockMvc iniciou async, faz o dispatch. */
    private MvcResult graphql(String authorization, String query) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("query", query));
        var request = post("/graphql").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body);
        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        MvcResult first = mockMvc.perform(request).andReturn();
        return first.getRequest().isAsyncStarted() ? mockMvc.perform(asyncDispatch(first)).andReturn() : first;
    }

    // ---------- Login, BCrypt e JWT ----------

    @Test
    @DisplayName("POST /auth/login com credenciais corretas retorna 200 e um token JWT")
    void loginReturnsToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "admin@teste.com", "password", PASSWORD));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("A senha é gravada como hash BCrypt, nunca em texto puro")
    void passwordIsStoredAsBcryptHash() {
        String stored = users.findByEmailIgnoreCase("admin@teste.com").orElseThrow().getPassword();

        assertNotEquals(PASSWORD, stored);
        assertTrue(stored.startsWith("$2"), "hash BCrypt começa com $2");
        assertTrue(encoder.matches(PASSWORD, stored));
    }

    @Test
    @DisplayName("POST /auth/login com senha errada retorna 401")
    void loginWrongPasswordReturns401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "admin@teste.com", "password", "errada123"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /auth/login com e-mail inexistente retorna 401")
    void loginUnknownUserReturns401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "ninguem@teste.com", "password", PASSWORD));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login sem senha retorna 400 (Bean Validation)")
    void loginWithoutPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@teste.com\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Cadastro ----------

    @Test
    @DisplayName("POST /auth/register como CLIENT retorna 201 e o token já permite consultar serviços")
    void registerClientReturns201AndTokenWorks() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Novo Cliente", "email", "novo@teste.com", "password", "Senha@123", "role", "CLIENT"));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get(SERVICES).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/register com papel ADMIN retorna 400")
    void registerAdminReturns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Invasor", "email", "invasor@teste.com", "password", "Senha@123", "role", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register com e-mail já cadastrado retorna 409")
    void registerDuplicateEmailReturns409() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Outro Admin", "email", "ADMIN@teste.com", "password", "Senha@123", "role", "CLIENT"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // ---------- Proteção de rotas e papéis ----------

    @Test
    @DisplayName("GET /api/v1/services sem token retorna 401")
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(SERVICES)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/services com token inválido retorna 401")
    void listWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get(SERVICES).header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CLIENT consulta (200) mas não cria serviço (403)")
    void clientCanReadButNotCreate() throws Exception {
        String client = bearer("client@teste.com");

        mockMvc.perform(get(SERVICES).header(HttpHeaders.AUTHORIZATION, client))
                .andExpect(status().isOk());
        mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, client)
                        .contentType(MediaType.APPLICATION_JSON).content(serviceJson("Serviço do cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER cria serviço (201) mas não remove (403)")
    void providerCanCreateButNotDelete() throws Exception {
        String provider = bearer("provider@teste.com");

        MvcResult created = mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, provider)
                        .contentType(MediaType.APPLICATION_JSON).content(serviceJson("Serviço do prestador")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete(SERVICES + "/" + id).header(HttpHeaders.AUTHORIZATION, provider))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN percorre o CRUD completo: POST 201, GET 200, PATCH 200, DELETE 204, GET 404")
    void adminFullCrudFlow() throws Exception {
        String admin = bearer("admin@teste.com");

        MvcResult created = mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(serviceJson("Instalação de ventilador")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get(SERVICES + "/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Instalação de ventilador"));

        mockMvc.perform(patch(SERVICES + "/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"price\":175.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(175.50));

        mockMvc.perform(delete(SERVICES + "/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(SERVICES + "/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST com título repetido (ignorando maiúsculas) retorna 409")
    void duplicateTitleReturns409() throws Exception {
        String admin = bearer("admin@teste.com");
        mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(serviceJson("Limpeza completa")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(serviceJson("LIMPEZA COMPLETA")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST com preço 0 retorna 400 com details (Bean Validation + handler global)")
    void invalidPriceReturns400() throws Exception {
        String admin = bearer("admin@teste.com");

        mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Serviço válido\",\"price\":0,\"category\":\"OTHER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field=='price')]").exists());
    }

    // ---------- Sanitização (XSS) ----------

    @Test
    @DisplayName("POST com <script> no título grava o texto sanitizado")
    void scriptInTitleIsSanitized() throws Exception {
        String admin = bearer("admin@teste.com");

        mockMvc.perform(post(SERVICES).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceJson("<script>alert(1)</script>Montagem de móveis")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Montagem de móveis"));
    }

    // ---------- GraphQL ----------

    @Test
    @DisplayName("POST /graphql sem token retorna 401")
    void graphqlWithoutTokenReturns401() throws Exception {
        MvcResult result = graphql(null, "{ services { totalElements } }");

        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("POST /graphql com token de CLIENT lista serviços com filtro e paginação")
    void graphqlListsServices() throws Exception {
        saveService("Limpeza de estofados");

        MvcResult result = graphql(bearer("client@teste.com"),
                "{ services(category: CLEANING, size: 5) { totalElements content { title price category active } } }");

        assertEquals(200, result.getResponse().getStatus());
        String json = result.getResponse().getContentAsString();
        assertEquals(1, (int) JsonPath.read(json, "$.data.services.totalElements"));
        assertEquals("Limpeza de estofados", JsonPath.read(json, "$.data.services.content[0].title"));
        assertEquals("CLEANING", JsonPath.read(json, "$.data.services.content[0].category"));
    }

    @Test
    @DisplayName("POST /graphql serviceById com id inexistente devolve erro NOT_FOUND")
    void graphqlServiceByIdNotFound() throws Exception {
        MvcResult result = graphql(bearer("admin@teste.com"), "{ serviceById(id: 999999) { id title } }");

        String json = result.getResponse().getContentAsString();
        assertEquals("NOT_FOUND", JsonPath.read(json, "$.errors[0].extensions.classification"));
    }
}
