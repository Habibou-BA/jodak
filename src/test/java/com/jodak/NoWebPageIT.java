package com.jodak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La plateforme n'expose que des API (REST et SOAP) et leur documentation : aucune page web n'est
 * servie. Ce test verrouille cette décision en vérifiant que les anciennes routes de consultation
 * ne répondent plus.
 */
@AutoConfigureMockMvc
class NoWebPageIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} → 404")
    @ValueSource(strings = {"/", "/dashboard", "/disciplines", "/athletes", "/epreuves", "/medailles"})
    @DisplayName("Aucune page web n'est accessible")
    void webPagesAreGone(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }
}
