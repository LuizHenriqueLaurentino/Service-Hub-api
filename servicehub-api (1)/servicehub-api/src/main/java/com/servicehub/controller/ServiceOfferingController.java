package com.servicehub.controller;

import com.servicehub.dto.ErrorResponse;
import com.servicehub.dto.PageResponse;
import com.servicehub.dto.ServiceOfferingPatchRequest;
import com.servicehub.dto.ServiceOfferingRequest;
import com.servicehub.dto.ServiceOfferingResponse;
import com.servicehub.model.ServiceCategory;
import com.servicehub.service.ServiceOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller REST de serviços. Apenas traduz HTTP para chamadas ao Service:
 * nenhuma regra de negócio fica aqui.
 */
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Serviços",
        description = "Cadastro, consulta, atualização e remoção de serviços oferecidos na plataforma ServiceHub")
public class ServiceOfferingController {

    private final ServiceOfferingService service;

    public ServiceOfferingController(ServiceOfferingService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Criar serviço",
            description = "Cadastra um novo serviço. O título deve ser único (sem diferenciar maiúsculas de "
                    + "minúsculas). Retorna 201 com o recurso criado e o header Location apontando para ele.",
            operationId = "createService")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um serviço com este título",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceOfferingResponse> create(@Valid @RequestBody ServiceOfferingRequest request) {
        ServiceOfferingResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(
            summary = "Listar serviços",
            description = "Retorna uma página de serviços ordenados por título. É possível filtrar por "
                    + "categoria e por status (ativo ou inativo). O tamanho máximo da página é 100.",
            operationId = "listServices")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de serviços retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de filtro inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<ServiceOfferingResponse>> findAll(
            @Parameter(description = "Filtra pela categoria do serviço", example = "PLUMBING")
            @RequestParam(required = false) ServiceCategory category,
            @Parameter(description = "Filtra por serviços ativos (true) ou inativos (false)", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página (máximo 100)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findAll(category, active, page, size));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar serviço por ID",
            description = "Retorna os dados completos de um serviço a partir do seu identificador único.",
            operationId = "getServiceById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceOfferingResponse> findById(
            @Parameter(description = "Identificador único do serviço", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Substituir serviço",
            description = "Substitui integralmente os dados de um serviço existente. Todos os campos "
                    + "obrigatórios devem ser enviados. Para alterar apenas alguns campos, use PATCH.",
            operationId = "replaceService")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outro serviço com este título",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceOfferingResponse> replace(
            @Parameter(description = "Identificador único do serviço", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ServiceOfferingRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualizar serviço parcialmente",
            description = "Altera apenas os campos enviados no corpo da requisição; os demais permanecem "
                    + "como estão. Útil, por exemplo, para desativar um serviço ou ajustar o preço.",
            operationId = "patchService")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfferingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outro serviço com este título",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceOfferingResponse> patch(
            @Parameter(description = "Identificador único do serviço", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ServiceOfferingPatchRequest request) {
        return ResponseEntity.ok(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover serviço",
            description = "Remove definitivamente um serviço. Retorna 204 sem corpo em caso de sucesso.",
            operationId = "deleteService")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador único do serviço", example = "1", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
