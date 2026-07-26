package com.jodak.controllers.web;

import com.jodak.dtos.athlete.AthleteSearchCriteria;
import com.jodak.enums.Gender;
import com.jodak.services.interfaces.AthleteService;
import com.jodak.services.interfaces.DisciplineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Vue de recherche et de liste des athlètes (filtres + pagination). Les paramètres de filtre sont
 * reçus en {@code String} et convertis, afin de tolérer les valeurs vides des liens de pagination.
 */
@Controller
@RequestMapping("/athletes")
@RequiredArgsConstructor
public class AthleteWebController {

    private final AthleteService athleteService;
    private final DisciplineService disciplineService;

    @GetMapping
    public String list(@RequestParam(required = false) String lastName,
                       @RequestParam(required = false) String gender,
                       @RequestParam(required = false) String disciplineId,
                       @PageableDefault(size = 12, sort = "lastName") Pageable pageable,
                       Model model) {
        AthleteSearchCriteria criteria = new AthleteSearchCriteria(
                blankToNull(lastName), null, parseGender(gender), null, parseLong(disciplineId),
                null, null);

        model.addAttribute("page", athleteService.search(criteria, pageable));
        model.addAttribute("disciplines", disciplineService.getAll(Pageable.unpaged()).content());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("filterLastName", blankToEmpty(lastName));
        model.addAttribute("filterGender", blankToEmpty(gender));
        model.addAttribute("filterDisciplineId", blankToEmpty(disciplineId));
        model.addAttribute("active", "athletes");
        return "athletes";
    }

    private Gender parseGender(String value) {
        return (value == null || value.isBlank()) ? null : Gender.valueOf(value);
    }

    private Long parseLong(String value) {
        return (value == null || value.isBlank()) ? null : Long.valueOf(value);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
