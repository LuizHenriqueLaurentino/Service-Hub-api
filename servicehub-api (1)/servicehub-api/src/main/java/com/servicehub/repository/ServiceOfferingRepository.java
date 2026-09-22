package com.servicehub.repository;

import com.servicehub.model.ServiceCategory;
import com.servicehub.model.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Acesso a dados de serviços. As consultas são derivadas do nome dos métodos (Query Methods).
 */
@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    Page<ServiceOffering> findByCategory(ServiceCategory category, Pageable pageable);

    Page<ServiceOffering> findByActive(boolean active, Pageable pageable);

    Page<ServiceOffering> findByCategoryAndActive(ServiceCategory category, boolean active, Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);
}
