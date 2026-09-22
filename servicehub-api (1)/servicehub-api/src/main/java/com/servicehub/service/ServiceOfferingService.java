package com.servicehub.service;

import com.servicehub.dto.PageResponse;
import com.servicehub.dto.ServiceOfferingPatchRequest;
import com.servicehub.dto.ServiceOfferingRequest;
import com.servicehub.dto.ServiceOfferingResponse;
import com.servicehub.exception.ConflictException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.ServiceCategory;
import com.servicehub.model.ServiceOffering;
import com.servicehub.repository.ServiceOfferingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Camada de negócio de serviços: concentra as regras (título único, valores padrão,
 * normalização de paginação) e orquestra o acesso ao Repository.
 */
@Service
public class ServiceOfferingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceOfferingRepository repository;

    public ServiceOfferingService(ServiceOfferingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServiceOfferingResponse create(ServiceOfferingRequest request) {
        String title = request.title().trim();
        if (repository.existsByTitleIgnoreCase(title)) {
            throw new ConflictException("Já existe um serviço com o título: " + title);
        }
        ServiceOffering entity = new ServiceOffering();
        applyFullData(entity, request, title);
        return ServiceOfferingResponse.fromEntity(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ServiceOfferingResponse findById(Long id) {
        return ServiceOfferingResponse.fromEntity(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceOfferingResponse> findAll(ServiceCategory category, Boolean active,
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "title"));

        Page<ServiceOffering> result;
        if (category != null && active != null) {
            result = repository.findByCategoryAndActive(category, active, pageable);
        } else if (category != null) {
            result = repository.findByCategory(category, pageable);
        } else if (active != null) {
            result = repository.findByActive(active, pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return PageResponse.from(result, ServiceOfferingResponse::fromEntity);
    }

    @Transactional
    public ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) {
        ServiceOffering entity = getOrThrow(id);
        String title = request.title().trim();
        if (repository.existsByTitleIgnoreCaseAndIdNot(title, id)) {
            throw new ConflictException("Já existe um serviço com o título: " + title);
        }
        applyFullData(entity, request, title);
        return ServiceOfferingResponse.fromEntity(repository.saveAndFlush(entity));
    }

    @Transactional
    public ServiceOfferingResponse patch(Long id, ServiceOfferingPatchRequest request) {
        ServiceOffering entity = getOrThrow(id);

        if (request.title() != null) {
            String title = request.title().trim();
            if (repository.existsByTitleIgnoreCaseAndIdNot(title, id)) {
                throw new ConflictException("Já existe um serviço com o título: " + title);
            }
            entity.setTitle(title);
        }
        if (request.description() != null) {
            entity.setDescription(request.description().trim());
        }
        if (request.price() != null) {
            entity.setPrice(request.price());
        }
        if (request.category() != null) {
            entity.setCategory(request.category());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        return ServiceOfferingResponse.fromEntity(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        ServiceOffering entity = getOrThrow(id);
        repository.delete(entity);
    }

    private ServiceOffering getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + id));
    }

    private void applyFullData(ServiceOffering entity, ServiceOfferingRequest request, String title) {
        entity.setTitle(title);
        entity.setDescription(request.description() == null ? null : request.description().trim());
        entity.setPrice(request.price());
        entity.setCategory(request.category());
        entity.setActive(request.active() == null || request.active());
    }
}
