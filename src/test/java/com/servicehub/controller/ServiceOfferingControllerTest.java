package com.servicehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.dto.PageResponse;
import com.servicehub.dto.ServiceOfferingPatchRequest;
import com.servicehub.dto.ServiceOfferingRequest;
import com.servicehub.dto.ServiceOfferingResponse;
import com.servicehub.exception.ConflictException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.ServiceCategory;
import com.servicehub.security.JwtService;
import com.servicehub.security.RestAccessDeniedHandler;
import com.servicehub.security.RestAuthenticationEntryPoint;
import com.servicehub.security.SecurityConfig;
import com.servicehub.service.ServiceOfferingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web: só o controller, o GlobalExceptionHandler e a SecurityConfig sobem;
 * o Service é simulado com @MockBean.
 */
@WebMvcTest(ServiceOfferingController.class)
@Import({SecurityConfig.class, JwtService.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class ServiceOfferingControllerTest {

    private static final String BASE = "/api/v1/services";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceOfferingService service;

    // Exigido pela SecurityConfig (AuthenticationManager); aqui não é usado.
    @MockBean
    private UserDetailsService userDetailsService;

    // Instanciado diretamente em vez de injetado (evita problemas de injeção do ObjectMapper nos testes)
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ServiceOfferingResponse sample() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 27, 10, 0);
        return new ServiceOfferingResponse(1L, "Instalação de ventilador de teto", "Fixação e teste",
                new BigDecimal("150.00"), ServiceCategory.ELECTRICAL, true, now, now);
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(new ServiceOfferingRequest(
                "Instalação de ventilador de teto", "Fixação e teste",
                new BigDecimal("150.00"), ServiceCategory.ELECTRICAL, true));
    }

    @Test
    @DisplayName("GET /api/v1/services sem token retorna 401")
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/v1/services como CLIENT retorna 200 com a página de serviços")
    void listAsClientReturns200() throws Exception {
        when(service.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(sample()), 0, 10, 1, 1));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Instalação de ventilador de teto"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/v1/services/1 retorna 200")
    void getByIdReturns200() throws Exception {
        when(service.findById(1L)).thenReturn(sample());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("ELECTRICAL"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/v1/services/999 retorna 404 no formato ErrorResponse")
    void getByIdNotFoundReturns404() throws Exception {
        when(service.findById(999L)).thenThrow(new ResourceNotFoundException("Serviço não encontrado: 999"));

        mockMvc.perform(get(BASE + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Serviço não encontrado: 999"))
                .andExpect(jsonPath("$.path").value(BASE + "/999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/services como ADMIN retorna 201 com header Location")
    void createAsAdminReturns201() throws Exception {
        when(service.create(any(ServiceOfferingRequest.class))).thenReturn(sample());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("POST /api/v1/services como PROVIDER retorna 201")
    void createAsProviderReturns201() throws Exception {
        when(service.create(any(ServiceOfferingRequest.class))).thenReturn(sample());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/v1/services como CLIENT retorna 403 e não chama o service")
    void createAsClientReturns403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(service, never()).create(any(ServiceOfferingRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/services com título vazio e preço 0 retorna 400 com details por campo")
    void createInvalidBodyReturns400() throws Exception {
        String invalid = "{\"title\":\"\",\"price\":0,\"category\":\"ELECTRICAL\"}";

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Falha na validação dos dados enviados."))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[?(@.field=='price')]").exists())
                .andExpect(jsonPath("$.details[?(@.field=='title')]").exists());

        verify(service, never()).create(any(ServiceOfferingRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/services com categoria inexistente retorna 400")
    void createInvalidCategoryReturns400() throws Exception {
        String invalid = "{\"title\":\"Serviço válido\",\"price\":10,\"category\":\"XYZ\"}";

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/services com título duplicado retorna 409")
    void createDuplicateReturns409() throws Exception {
        when(service.create(any(ServiceOfferingRequest.class)))
                .thenThrow(new ConflictException("Já existe um serviço com o título: Instalação de ventilador de teto"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("PATCH /api/v1/services/1 como PROVIDER retorna 200")
    void patchAsProviderReturns200() throws Exception {
        when(service.patch(eq(1L), any(ServiceOfferingPatchRequest.class))).thenReturn(sample());

        mockMvc.perform(patch(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"price\":175.50}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("DELETE /api/v1/services/1 como PROVIDER retorna 403")
    void deleteAsProviderReturns403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden());

        verify(service, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/services/1 como ADMIN retorna 204")
    void deleteAsAdminReturns204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
