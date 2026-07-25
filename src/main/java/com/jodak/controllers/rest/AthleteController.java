package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.athlete.AthletePatchRequest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.dtos.athlete.AthleteSearchCriteria;
import com.jodak.dtos.common.PageResponse;
import com.jodak.services.interfaces.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Points d'entrée REST de gestion des athlètes.
 */
@RestController
@RequestMapping(ApiPaths.ATHLETES)
@RequiredArgsConstructor
@Tag(name = "Athlètes", description = "Gestion des athlètes")
public class AthleteController {

    private final AthleteService service;

    @PostMapping
    @Operation(summary = "Créer un athlète")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Athlète créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Nationalité ou discipline introuvable")
    })
    public ResponseEntity<AthleteResponse> create(@Valid @RequestBody AthleteRequest request) {
        AthleteResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un athlète par identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlète trouvé"),
            @ApiResponse(responseCode = "404", description = "Athlète introuvable")
    })
    public AthleteResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Rechercher des athlètes (multicritère, paginé et trié)")
    public PageResponse<AthleteResponse> search(
            @ParameterObject AthleteSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return service.search(criteria, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier complètement un athlète")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlète modifié"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Athlète, nationalité ou discipline introuvable")
    })
    public AthleteResponse update(@PathVariable Long id, @Valid @RequestBody AthleteRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier partiellement un athlète")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlète modifié"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Athlète, nationalité ou discipline introuvable")
    })
    public AthleteResponse patch(@PathVariable Long id, @Valid @RequestBody AthletePatchRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un athlète")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Athlète supprimé"),
            @ApiResponse(responseCode = "404", description = "Athlète introuvable"),
            @ApiResponse(responseCode = "409", description = "Athlète référencé par des résultats")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
