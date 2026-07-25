package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.dtos.epreuve.EpreuveSearchCriteria;
import com.jodak.dtos.resultat.PodiumResponse;
import com.jodak.services.interfaces.EpreuveService;
import com.jodak.services.interfaces.ResultatService;
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
 * Points d'entrée REST de gestion des épreuves.
 */
@RestController
@RequestMapping(ApiPaths.EPREUVES)
@RequiredArgsConstructor
@Tag(name = "Épreuves", description = "Gestion des épreuves")
public class EpreuveController {

    private final EpreuveService service;
    private final ResultatService resultatService;

    @PostMapping
    @Operation(summary = "Créer une épreuve")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Épreuve créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Discipline introuvable"),
            @ApiResponse(responseCode = "409", description = "Épreuve déjà existante (libellé, discipline, date)")
    })
    public ResponseEntity<EpreuveResponse> create(@Valid @RequestBody EpreuveRequest request) {
        EpreuveResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une épreuve par identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Épreuve trouvée"),
            @ApiResponse(responseCode = "404", description = "Épreuve introuvable")
    })
    public EpreuveResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Rechercher des épreuves (par discipline, par date, paginé et trié)")
    public PageResponse<EpreuveResponse> search(
            @ParameterObject EpreuveSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "eventDate") Pageable pageable) {
        return service.search(criteria, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une épreuve")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Épreuve modifiée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Épreuve ou discipline introuvable"),
            @ApiResponse(responseCode = "409", description = "Épreuve déjà existante (libellé, discipline, date)")
    })
    public EpreuveResponse update(@PathVariable Long id, @Valid @RequestBody EpreuveRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une épreuve")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Épreuve supprimée"),
            @ApiResponse(responseCode = "404", description = "Épreuve introuvable"),
            @ApiResponse(responseCode = "409", description = "Épreuve référencée par des résultats")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/podium")
    @Operation(summary = "Consulter le podium d'une épreuve")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Podium de l'épreuve"),
            @ApiResponse(responseCode = "404", description = "Épreuve introuvable")
    })
    public PodiumResponse getPodium(@PathVariable Long id) {
        return resultatService.getPodium(id);
    }
}
