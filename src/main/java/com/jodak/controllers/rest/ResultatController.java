package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.dtos.resultat.ResultatSearchCriteria;
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
 * Points d'entrée REST de gestion des résultats. La médaille est attribuée automatiquement.
 */
@RestController
@RequestMapping(ApiPaths.RESULTATS)
@RequiredArgsConstructor
@Tag(name = "Résultats", description = "Enregistrement des résultats et attribution des médailles")
public class ResultatController {

    private final ResultatService service;

    @PostMapping
    @Operation(summary = "Enregistrer un résultat (médaille attribuée automatiquement)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Résultat enregistré"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Épreuve ou athlète introuvable"),
            @ApiResponse(responseCode = "409", description = "Athlète ou rang déjà attribué pour l'épreuve"),
            @ApiResponse(responseCode = "422", description = "L'athlète ne pratique pas la discipline de l'épreuve")
    })
    public ResponseEntity<ResultatResponse> create(@Valid @RequestBody ResultatRequest request) {
        ResultatResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un résultat par identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultat trouvé"),
            @ApiResponse(responseCode = "404", description = "Résultat introuvable")
    })
    public ResultatResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Rechercher des résultats (par épreuve, athlète, médaille — paginé)")
    public PageResponse<ResultatResponse> search(
            @ParameterObject ResultatSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "rankPosition") Pageable pageable) {
        return service.search(criteria, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Corriger un résultat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultat modifié"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Résultat, épreuve ou athlète introuvable"),
            @ApiResponse(responseCode = "409", description = "Athlète ou rang déjà attribué pour l'épreuve"),
            @ApiResponse(responseCode = "422", description = "L'athlète ne pratique pas la discipline de l'épreuve")
    })
    public ResultatResponse update(@PathVariable Long id, @Valid @RequestBody ResultatRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un résultat")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Résultat supprimé"),
            @ApiResponse(responseCode = "404", description = "Résultat introuvable")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
