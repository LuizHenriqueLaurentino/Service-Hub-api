package com.servicehub.service;

import com.servicehub.dto.PageResponse;
import com.servicehub.dto.ServiceOfferingPatchRequest;
import com.servicehub.dto.ServiceOfferingRequest;
import com.servicehub.dto.ServiceOfferingResponse;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.ConflictException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.ServiceCategory;
import com.servicehub.model.ServiceOffering;
import com.servicehub.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock
    private ServiceOfferingRepository repository;

    @InjectMocks
    private ServiceOfferingService service;

    private ServiceOffering existing(Long id, String title) {
        ServiceOffering entity = new ServiceOffering();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setTitle(title);
        entity.setDescription("Descrição original");
        entity.setPrice(new BigDecimal("100.00"));
        entity.setCategory(ServiceCategory.CLEANING);
        entity.setActive(true);
        return entity;
    }

    @Test
    @DisplayName("create: salva o serviço, remove espaços do título e assume active=true quando omitido")
    void createSavesService() {
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "  Limpeza de estofados  ", "Limpeza a seco", new BigDecimal("200.00"),
                ServiceCategory.CLEANING, null);
        when(repository.existsByTitleIgnoreCase("Limpeza de estofados")).thenReturn(false);
        when(repository.save(any(ServiceOffering.class))).thenAnswer(invocation -> {
            ServiceOffering saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });

        ServiceOfferingResponse response = service.create(request);

        assertEquals(10L, response.id());
        assertEquals("Limpeza de estofados", response.title());
        assertEquals(new BigDecimal("200.00"), response.price());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("create: título duplicado lança ConflictException e não salva")
    void createDuplicateTitleThrowsConflict() {
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "Limpeza residencial completa", null, new BigDecimal("250.00"),
                ServiceCategory.CLEANING, true);
        when(repository.existsByTitleIgnoreCase("Limpeza residencial completa")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));

        verify(repository, never()).save(any(ServiceOffering.class));
    }

    @Test
    @DisplayName("findById: id inexistente lança ResourceNotFoundException")
    void findByIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    @DisplayName("update: substitui todos os campos do serviço")
    void updateReplacesAllFields() {
        ServiceOffering entity = existing(1L, "Título antigo");
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "Título novo", "Descrição nova", new BigDecimal("300.00"), ServiceCategory.PAINTING, false);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByTitleIgnoreCaseAndIdNot("Título novo", 1L)).thenReturn(false);
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        ServiceOfferingResponse response = service.update(1L, request);

        assertEquals("Título novo", response.title());
        assertEquals("Descrição nova", response.description());
        assertEquals(new BigDecimal("300.00"), response.price());
        assertEquals(ServiceCategory.PAINTING, response.category());
        assertFalse(response.active());
    }

    @Test
    @DisplayName("update: título que já pertence a outro serviço lança ConflictException")
    void updateConflictWithOtherService() {
        ServiceOffering entity = existing(1L, "Título antigo");
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "Título de outro serviço", null, new BigDecimal("50.00"), ServiceCategory.OTHER, true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByTitleIgnoreCaseAndIdNot("Título de outro serviço", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(1L, request));

        verify(repository, never()).saveAndFlush(any(ServiceOffering.class));
    }

    @Test
    @DisplayName("patch: altera somente os campos enviados")
    void patchChangesOnlyProvidedFields() {
        ServiceOffering entity = existing(1L, "Título original");
        ServiceOfferingPatchRequest request = new ServiceOfferingPatchRequest(
                null, null, new BigDecimal("175.50"), null, false);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        ServiceOfferingResponse response = service.patch(1L, request);

        assertEquals("Título original", response.title());
        assertEquals("Descrição original", response.description());
        assertEquals(ServiceCategory.CLEANING, response.category());
        assertEquals(new BigDecimal("175.50"), response.price());
        assertFalse(response.active());
    }

    @Test
    @DisplayName("delete: remove o serviço existente")
    void deleteRemovesExistingService() {
        ServiceOffering entity = existing(1L, "Título");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.delete(1L);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete: id inexistente lança ResourceNotFoundException")
    void deleteNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));

        verify(repository, never()).delete(any(ServiceOffering.class));
    }

    @Test
    @DisplayName("findAll: normaliza página negativa e limita o tamanho máximo a 100")
    void findAllNormalizesPagination() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        PageResponse<ServiceOfferingResponse> response = service.findAll(null, null, -5, 5000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
        assertTrue(response.content().isEmpty());
    }

    @Test
    @DisplayName("create: remove tags HTML e scripts do título e da descrição (sanitização contra XSS)")
    void createSanitizesHtmlAndScripts() {
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "<b>Pintura</b> de parede", "<script>alert('xss')</script>Tinta lavável",
                new BigDecimal("300.00"), ServiceCategory.PAINTING, true);
        when(repository.existsByTitleIgnoreCase("Pintura de parede")).thenReturn(false);
        when(repository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOfferingResponse response = service.create(request);

        assertEquals("Pintura de parede", response.title());
        assertEquals("Tinta lavável", response.description());
    }

    @Test
    @DisplayName("create: título que fica vazio após a sanitização lança BadRequestException e não salva")
    void createBlankTitleAfterSanitizeThrowsBadRequest() {
        ServiceOfferingRequest request = new ServiceOfferingRequest(
                "<b></b><i></i>", null, new BigDecimal("50.00"), ServiceCategory.OTHER, true);

        assertThrows(BadRequestException.class, () -> service.create(request));

        verify(repository, never()).save(any(ServiceOffering.class));
    }
}
