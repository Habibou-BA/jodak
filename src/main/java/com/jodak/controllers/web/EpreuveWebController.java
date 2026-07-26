package com.jodak.controllers.web;

import com.jodak.dtos.epreuve.EpreuveSearchCriteria;
import com.jodak.services.interfaces.DisciplineService;
import com.jodak.services.interfaces.EpreuveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Vue de recherche et de liste des épreuves (filtre par discipline + pagination).
 */
@Controller
@RequestMapping("/epreuves")
@RequiredArgsConstructor
public class EpreuveWebController {

    private final EpreuveService epreuveService;
    private final DisciplineService disciplineService;

    @GetMapping
    public String list(@RequestParam(required = false) String disciplineId,
                       @PageableDefault(size = 12, sort = "eventDate") Pageable pageable,
                       Model model) {
        EpreuveSearchCriteria criteria =
                new EpreuveSearchCriteria(null, parseLong(disciplineId), null, null, null);

        model.addAttribute("page", epreuveService.search(criteria, pageable));
        model.addAttribute("disciplines", disciplineService.getAll(Pageable.unpaged()).content());
        model.addAttribute("filterDisciplineId", disciplineId == null ? "" : disciplineId);
        model.addAttribute("active", "epreuves");
        return "epreuves";
    }

    private Long parseLong(String value) {
        return (value == null || value.isBlank()) ? null : Long.valueOf(value);
    }
}
