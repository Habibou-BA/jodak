package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.services.interfaces.DisciplineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * Points d'entrée REST de gestion des disciplines.
 */
@RestController
@RequestMapping(ApiPaths.DISCIPLINES)
@RequiredArgsConstructor
@Tag(name = "Disciplines", description = "Gestion des disciplines olympiques")
public class DisciplineController {

    private final DisciplineService service;

    @PostMapping
    @Operation(summary = "Créer une discipline")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Discipline créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "409", description = "Nom de discipline déjà utilisé")
    })
    public ResponseEntity<DisciplineResponse> create(@Valid @RequestBody DisciplineRequest request) {
        DisciplineResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une discipline par identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discipline trouvée"),
            @ApiResponse(responseCode = "404", description = "Discipline introuvable")
    })
    public DisciplineResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lister les disciplines (paginé et trié)")
    public PageResponse<DisciplineResponse> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.getAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une discipline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discipline modifiée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Discipline introuvable"),
            @ApiResponse(responseCode = "409", description = "Nom de discipline déjà utilisé")
    })
    public DisciplineResponse update(@PathVariable Long id,
                                     @Valid @RequestBody DisciplineRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une discipline")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Discipline supprimée"),
            @ApiResponse(responseCode = "404", description = "Discipline introuvable"),
            @ApiResponse(responseCode = "409", description = "Discipline référencée par des athlètes ou épreuves")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
