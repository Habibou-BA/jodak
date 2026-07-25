package com.jodak.controllers.rest;

import com.jodak.constants.ApiPaths;
import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.services.interfaces.MedalTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Point d'entrée REST du tableau officiel des médailles.
 */
@RestController
@RequestMapping(ApiPaths.MEDAL_TABLE)
@RequiredArgsConstructor
@Tag(name = "Tableau des médailles", description = "Classement officiel des nations par médailles")
public class MedalTableController {

    private final MedalTableService service;

    @GetMapping
    @Operation(summary = "Calculer le tableau officiel des médailles "
            + "(classement or, puis argent, puis bronze)")
    public List<MedalTableRowResponse> getMedalTable() {
        return service.getMedalTable();
    }
}
