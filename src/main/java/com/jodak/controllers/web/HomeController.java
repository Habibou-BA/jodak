package com.jodak.controllers.web;

import com.jodak.services.interfaces.DashboardService;
import com.jodak.services.interfaces.MedalTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page d'accueil (tableau de bord visuel). Réutilise les services applicatifs.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;
    private final MedalTableService medalTableService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard());
        model.addAttribute("medalTable", medalTableService.getMedalTable());
        model.addAttribute("active", "home");
        return "index";
    }
}
