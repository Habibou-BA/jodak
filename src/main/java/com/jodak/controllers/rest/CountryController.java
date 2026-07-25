package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.country.CountryRequest;
import com.jodak.dtos.country.CountryResponse;
import com.jodak.services.interfaces.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Points d'entrée REST du référentiel des nations.
 */
@RestController
@RequestMapping(ApiPaths.COUNTRIES)
@RequiredArgsConstructor
@Tag(name = "Nations", description = "Référentiel des nations")
public class CountryController {

    private final CountryService service;

    @PostMapping
    @Operation(summary = "Ajouter une nation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nation créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "409", description = "Code ou nom déjà utilisé")
    })
    public ResponseEntity<CountryResponse> create(@Valid @RequestBody CountryRequest request) {
        CountryResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une nation par identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nation trouvée"),
            @ApiResponse(responseCode = "404", description = "Nation introuvable")
    })
    public CountryResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lister les nations (paginé et trié)")
    public PageResponse<CountryResponse> getAll(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        return service.getAll(pageable);
    }
}
