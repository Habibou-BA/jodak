package com.jodak.admin;

import com.jodak.admin.controllers.web.AdminConsoleController;
import com.jodak.admin.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Les pages de la console d'administration sont publiques (coques sans donnée sensible) ;
 * la protection est appliquée au niveau de l'API.
 */
@WebMvcTest(AdminConsoleController.class)
@Import(SecurityConfig.class)
class AdminConsoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La racine redirige vers la page de connexion")
    void rootRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/backoffice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/backoffice/login"));
    }

    @Test
    @DisplayName("La page de connexion s'affiche")
    void loginPageRenders() throws Exception {
        mockMvc.perform(get("/backoffice/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    @DisplayName("Le tableau de bord admin s'affiche")
    void dashboardPageRenders() throws Exception {
        mockMvc.perform(get("/backoffice/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }
}
