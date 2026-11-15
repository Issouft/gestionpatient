package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Invitation de l'administrateur d'un établissement par le super-administrateur.
 * Chaque test est annulé à la fin (@Transactional). EmailService est un faux : on récupère le jeton du lien.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdministrateurInvitationTest {

    private static final String EMAIL_ADMIN = "admin.invite@test.invalid";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionsDeTest sessions;

    @MockitoBean
    private EmailService emailService;

    private String jetonSuperAdmin;
    private Etablissement clinique;

    @BeforeEach
    void preparer() {
        Utilisateur superAdmin = creer(SuperAdministrateur.builder().email("super.admin.invite@test.invalid"));
        jetonSuperAdmin = jwtService.genererJeton(superAdmin.getEmail());
        clinique = sessions.creerClinique("Clinique Invitation");
    }

    @Test
    void lAdministrateurInviteActiveSonCompteEtTravailleDansSonEtablissement() throws Exception {
        // 1. Invitation : compte inactif, rôle ADMINISTRATEUR, email normalisé
        inviter(clinique.getId(), "Admin.Invite@Test.INVALID")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL_ADMIN))
                .andExpect(jsonPath("$.role").value("ADMINISTRATEUR"))
                .andExpect(jsonPath("$.actif").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());

        // 2. Le lien d'activation part à la bonne adresse
        ArgumentCaptor<String> jetonActivation = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyerInvitation(eq(EMAIL_ADMIN), eq("Awa"), jetonActivation.capture());

        // 3. Il choisit son mot de passe, se connecte : son établissement est choisi d'office
        mockMvc.perform(post("/auth/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jeton\":\"" + jetonActivation.getValue() + "\",\"motDePasse\":\"mot-de-passe-admin\"}"))
                .andExpect(status().isNoContent());
        String reponse = mockMvc.perform(post("/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL_ADMIN + "\",\"motDePasse\":\"mot-de-passe-admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(clinique.getId()))
                .andReturn().getResponse().getContentAsString();

        // 4. Il gère le personnel de son établissement
        String jetonAdmin = JsonPath.read(reponse, "$.jeton");
        mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jetonAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void unEmailDejaPrisDonne409SansEmail() throws Exception {
        creer(Patient.builder().email(EMAIL_ADMIN));

        inviter(clinique.getId(), EMAIL_ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_DEJA_UTILISE"));
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
    }

    @Test
    void unEtablissementInconnuDonne404SansCompteNiEmail() throws Exception {
        inviter(999_999_999, EMAIL_ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_INTROUVABLE"));
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
        assertThat(utilisateurRepository.existsByEmail(EMAIL_ADMIN)).isFalse();
    }

    @Test
    void desDonneesIncompletesDonnent400() throws Exception {
        mockMvc.perform(post("/etablissements/" + clinique.getId() + "/administrateurs")
                        .header("Authorization", "Bearer " + jetonSuperAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pas-un-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"))
                .andExpect(jsonPath("$.erreurs.nom").exists())
                .andExpect(jsonPath("$.erreurs.prenom").exists())
                .andExpect(jsonPath("$.erreurs.email").exists());
    }

    @Test
    void unAdministrateurNePeutPasInviterUnAutreAdministrateur() throws Exception {
        String jetonAdmin = sessions.jeton(creer(Administrateur.builder().email("admin.existant@test.invalid")), clinique);

        mockMvc.perform(post("/etablissements/" + clinique.getId() + "/administrateurs")
                        .header("Authorization", "Bearer " + jetonAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(EMAIL_ADMIN)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
    }

    // ---------- Outils ----------

    private ResultActions inviter(Integer etablissementId, String email) throws Exception {
        return mockMvc.perform(post("/etablissements/" + etablissementId + "/administrateurs")
                .header("Authorization", "Bearer " + jetonSuperAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(email)));
    }

    private String corps(String email) {
        return "{\"nom\":\"Diop\",\"prenom\":\"Awa\",\"email\":\"" + email + "\"}";
    }

    /** Le rôle est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Invitation")
                .password("non-utilise")
                .actif(true)
                .build());
    }
}
