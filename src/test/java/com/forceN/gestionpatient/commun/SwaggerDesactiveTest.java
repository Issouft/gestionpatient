package com.forceN.gestionpatient.commun;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** En production (SWAGGER_ACTIF=false), la page Swagger et la liste des routes n'existent plus. */
@SpringBootTest(properties = "SWAGGER_ACTIF=false")
@AutoConfigureMockMvc
class SwaggerDesactiveTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sansSwaggerLaListeDesRoutesNExistePas() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }
}
