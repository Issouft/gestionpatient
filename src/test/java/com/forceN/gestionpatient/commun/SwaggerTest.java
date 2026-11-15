package com.forceN.gestionpatient.commun;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La page Swagger s'ouvre sans jeton, mais les routes de l'API restent protégées. */
@SpringBootTest
@AutoConfigureMockMvc
class SwaggerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void laPageSwaggerSOuvreSansJeton() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        // La page lit sa configuration ici avant d'afficher les routes.
        mockMvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
    }

    @Test
    void laDescriptionDeLApiContientLeJetonEtLesGroupes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.jeton.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/auth/connexion'].post.tags", hasItem("Authentification")))
                .andExpect(jsonPath("$.paths['/etablissements/{etablissementId}/administrateurs'].post.tags",
                        hasItem("Établissements")))
                .andExpect(jsonPath("$.paths['/personnel'].get.tags", hasItem("Personnel")));
    }

    @Test
    void lesRoutesDeLApiRestentProtegees() throws Exception {
        mockMvc.perform(get("/personnel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }
}
