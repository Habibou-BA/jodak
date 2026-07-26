package com.jodak.admin.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Console web d'administration (Thymeleaf) sous {@code /backoffice}. Les pages sont des coques :
 * elles ne rendent aucune donnée sensible côté serveur et consomment l'API d'administration en
 * JavaScript avec le jeton JWT. La protection reste appliquée au niveau de l'API ({@code ROLE_ADMIN}).
 *
 * <p>Non référencé depuis le site public (isolation). Le chemin peut être remplacé par un
 * reverse-proxy si une URL non devinable est souhaitée (défense en profondeur).</p>
 */
@Controller
@RequestMapping("/backoffice")
public class AdminConsoleController {

    @GetMapping
    public String home() {
        return "redirect:/backoffice/login";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/imports")
    public String imports() {
        return "admin/imports";
    }

    @GetMapping("/maintenance")
    public String maintenance() {
        return "admin/maintenance";
    }

    @GetMapping("/logs")
    public String logs() {
        return "admin/logs";
    }
}
