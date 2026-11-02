package com.servicehub.graphql;

import com.servicehub.dto.PageResponse;
import com.servicehub.dto.ServiceOfferingResponse;
import com.servicehub.model.ServiceCategory;
import com.servicehub.service.ServiceOfferingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

/**
 * Endpoint alternativo GraphQL (POST /graphql), definido em graphql/schema.graphqls.
 * Reaproveita o mesmo ServiceOfferingService do REST e exige o mesmo token JWT.
 */
@Controller
public class ServiceGraphQLController {

    private final ServiceOfferingService service;

    public ServiceGraphQLController(ServiceOfferingService service) {
        this.service = service;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<ServiceOfferingResponse> services(@Argument ServiceCategory category,
                                                          @Argument Boolean active,
                                                          @Argument Integer page,
                                                          @Argument Integer size) {
        return service.findAll(category, active, page == null ? 0 : page, size == null ? 10 : size);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ServiceOfferingResponse serviceById(@Argument Long id) {
        return service.findById(id);
    }
}
