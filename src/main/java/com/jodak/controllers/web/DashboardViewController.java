package com.jodak.controllers.web;

import com.jodak.services.interfaces.DashboardService;
import com.jodak.services.interfaces.MedalTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Tableau de bord visuel (statistiques, tableau des médailles, classement par points).
 * Réutilise les services applicatifs.
 */
@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final DashboardService dashboardService;
    private final MedalTableService medalTableService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard());
        model.addAttribute("medalTable", medalTableService.getMedalTable());
        model.addAttribute("active", "dashboard");
        return "dashboard";
    }
}
