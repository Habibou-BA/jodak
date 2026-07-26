package com.jodak.controllers.web;

import com.jodak.services.interfaces.DisciplineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Vue de la liste des disciplines (paginée).
 */
@Controller
@RequestMapping("/disciplines")
@RequiredArgsConstructor
public class DisciplineWebController {

    private final DisciplineService disciplineService;

    @GetMapping
    public String list(@PageableDefault(size = 12, sort = "name") Pageable pageable, Model model) {
        model.addAttribute("page", disciplineService.getAll(pageable));
        model.addAttribute("active", "disciplines");
        return "disciplines";
    }
}
