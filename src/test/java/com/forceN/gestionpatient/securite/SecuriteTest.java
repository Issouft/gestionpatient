package com.forceN.gestionpatient.securite;

import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecuriteTest.ControleurDeTest.class)
class SecuriteTest {

    private static final String EMAIL = "patient.test.securite@example.com";
    private static final String MOT_DE_PASSE = "mot-de-passe-de-test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void routeProtegeeSansJetonDonne401AuFormatCommun() throws Exception {
        mockMvc.perform(get("/test-securite/connecte"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void jetonFalsifieDonne401() throws Exception {
        mockMvc.perform(get("/test-securite/connecte").header("Authorization", "Bearer faux.jeton.test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void connexionDonneUnJetonQuiOuvreLesRoutesProtegees() throws Exception {
        creerPatient(true);

        connecter(EMAIL, MOT_DE_PASSE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jeton", not(emptyOrNullString())));

        mockMvc.perform(get("/test-securite/connecte").header("Authorization", "Bearer " + jwtService.genererJeton(EMAIL)))
                .andExpect(status().isOk());
    }

    @Test
    void mauvaisMotDePasseEtEmailInconnuDonnentLaMemeReponse() throws Exception {
        creerPatient(true);

        verifierIdentifiantsInvalides(connecter(EMAIL, "mauvais-mot-de-passe"));
        verifierIdentifiantsInvalides(connecter("inconnu.test.securite@example.com", MOT_DE_PASSE));
    }

    @Test
    void motDePasseAbsentDonne401() throws Exception {
        creerPatient(true);

        verifierIdentifiantsInvalides(mockMvc.perform(post("/auth/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\"}")));
    }

    @Test
    void compteDesactiveNePeutNiSeConnecterNiUtiliserSonJeton() throws Exception {
        creerPatient(false);

        verifierIdentifiantsInvalides(connecter(EMAIL, MOT_DE_PASSE));

        mockMvc.perform(get("/test-securite/connecte").header("Authorization", "Bearer " + jwtService.genererJeton(EMAIL)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleInsuffisantDonne403AuFormatCommun() throws Exception {
        creerPatient(true);

        mockMvc.perform(get("/test-securite/medecin").header("Authorization", "Bearer " + jwtService.genererJeton(EMAIL)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    private ResultActions connecter(String email, String motDePasse) throws Exception {
        return mockMvc.perform(post("/auth/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}"));
    }

    private void verifierIdentifiantsInvalides(ResultActions resultat) throws Exception {
        resultat.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"))
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect."));
    }

    private void creerPatient(boolean actif) {
        utilisateurRepository.saveAndFlush(Patient.builder()
                .nom("Test")
                .prenom("Securite")
                .email(EMAIL)
                .password(passwordEncoder.encode(MOT_DE_PASSE))
                .actif(actif)
                .build());
    }

    @RestController
    static class ControleurDeTest {

        @GetMapping("/test-securite/connecte")
        String connecte() {
            return "ok";
        }

        @GetMapping("/test-securite/medecin")
        @PreAuthorize("hasRole('MEDECIN')")
        public String medecin() {
            return "ok";
        }
    }
}
