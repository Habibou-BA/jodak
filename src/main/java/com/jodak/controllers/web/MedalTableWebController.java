package com.jodak.controllers.web;

import com.jodak.services.interfaces.DashboardService;
import com.jodak.services.interfaces.MedalTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Vue du tableau des médailles et du classement par points.
 */
@Controller
@RequestMapping("/medailles")
@RequiredArgsConstructor
public class MedalTableWebController {

    private final MedalTableService medalTableService;
    private final DashboardService dashboardService;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("medalTable", medalTableService.getMedalTable());
        model.addAttribute("pointsRanking", dashboardService.getPointsRanking());
        model.addAttribute("active", "medailles");
        return "medailles";
    }
}
