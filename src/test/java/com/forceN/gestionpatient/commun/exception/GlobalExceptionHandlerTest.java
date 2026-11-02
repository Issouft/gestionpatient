package com.forceN.gestionpatient.commun.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControleurDeTest())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void erreurDeDomaineDonneSonStatutEtSonCode() throws Exception {
        mockMvc.perform(get("/erreur-domaine"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Erreur de test"))
                .andExpect(jsonPath("$.detail").value("Détail de test"))
                .andExpect(jsonPath("$.code").value("ERREUR_TEST"))
                .andExpect(jsonPath("$.instance").value("/erreur-domaine"));
    }

    @Test
    void exceptionHeriteeEstPriseEnChargeSansModifierLeGestionnaire() throws Exception {
        mockMvc.perform(get("/exception-heritee"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERREUR_TEST"))
                .andExpect(jsonPath("$.detail").value("Détail hérité"));
    }

    @Test
    void erreurImprevueNeRevelePasSonDetail() throws Exception {
        mockMvc.perform(get("/erreur-imprevue"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
                .andExpect(content().string(not(containsString("secret"))));
    }

    @Test
    void erreurDeSpringGardeSonStatut() throws Exception {
        mockMvc.perform(post("/erreur-domaine"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Getter
    @RequiredArgsConstructor
    private enum ErreurDeTest implements CodeErreur {
        ERREUR_TEST(HttpStatus.CONFLICT, "Erreur de test");

        private final HttpStatus statut;
        private final String titre;
    }

    private static class ExceptionHeritee extends ApplicationException {
        ExceptionHeritee() {
            super(ErreurDeTest.ERREUR_TEST, "Détail hérité");
        }
    }

    @RestController
    private static class ControleurDeTest {

        @GetMapping("/erreur-domaine")
        void erreurDomaine() {
            throw new ApplicationException(ErreurDeTest.ERREUR_TEST, "Détail de test");
        }

        @GetMapping("/exception-heritee")
        void exceptionHeritee() {
            throw new ExceptionHeritee();
        }

        @GetMapping("/erreur-imprevue")
        void erreurImprevue() {
            throw new IllegalStateException("secret");
        }
    }
}
