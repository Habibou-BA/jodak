package com.jodak.controllers.web;

import com.jodak.services.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page d'accueil de la plateforme : présentation des Jeux Olympiques de la Jeunesse de
 * Dakar 2026. Quelques compteurs en direct proviennent du tableau de bord.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard());
        model.addAttribute("active", "home");
        return "home";
    }
}
