package com.forceN.gestionpatient.securite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Avant d'appeler l'API depuis une autre adresse, le navigateur demande la permission (requête OPTIONS).
 * Seul le frontend configuré (application.url-frontend, http://localhost:4200 par défaut) la reçoit.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void leFrontendPeutAppelerLApi() throws Exception {
        demanderLaPermission("http://localhost:4200")
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void unAutreSiteNePeutPasAppelerLApi() throws Exception {
        demanderLaPermission("https://site-inconnu.example")
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    private ResultActions demanderLaPermission(String origine) throws Exception {
        return mockMvc.perform(options("/auth/connexion")
                .header("Origin", origine)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"));
    }
}
