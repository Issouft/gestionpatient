package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le personnel est cloisonné par établissement : l'administrateur de la clinique A ne voit ni ne touche
 * le personnel de la clinique B. Chaque test est annulé à la fin (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PersonnelEtablissementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AffiliationRepository affiliationRepository;

    @Autowired
    private SessionsDeTest sessions;

    // Aucun email ne doit partir pendant les tests.
    @MockitoBean
    private EmailService emailService;

    private Etablissement cliniqueA;
    private Etablissement cliniqueB;
    private String jetonAdminA;
    private Utilisateur secretaireB;

    @BeforeEach
    void preparerDeuxCliniques() {
        cliniqueA = sessions.creerClinique("Clinique A");
        cliniqueB = sessions.creerClinique("Clinique B");
        jetonAdminA = sessions.jeton(creer(Administrateur.builder().email("admin.a@test.invalid"), true), cliniqueA);
        // Compte jamais activé (inactif) : on pourrait lui renvoyer une invitation.
        secretaireB = creer(Secretaire.builder().email("secretaire.b@test.invalid"), false);
        sessions.affilier(secretaireB, cliniqueB);
    }

    @Test
    void lePersonnelCreeEstRattacheALEtablissementDeLAdministrateur() throws Exception {
        String reponse = mockMvc.perform(post("/personnel")
                        .header("Authorization", "Bearer " + jetonAdminA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Ndiaye","prenom":"Fatou","email":"fatou.a@test.invalid","role":"SECRETAIRE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(reponse, "$.id");

        assertThat(affiliationRepository.findByUtilisateurIdAndStatut(id, StatutAffiliation.ACTIVE))
                .singleElement()
                .satisfies(affiliation -> {
                    assertThat(affiliation.getEtablissement().getId()).isEqualTo(cliniqueA.getId());
                    assertThat(affiliation.getRole()).isEqualTo(Role.SECRETAIRE);
                });
        mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jetonAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem("fatou.a@test.invalid")));
    }

    @Test
    void laListeNeMontrePasLePersonnelDUnAutreEtablissement() throws Exception {
        mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jetonAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", not(hasItem("secretaire.b@test.invalid"))));
    }

    @Test
    void onNeDesactivePasLePersonnelDUnAutreEtablissement() throws Exception {
        secretaireB.setActif(true);
        utilisateurRepository.saveAndFlush(secretaireB);

        mockMvc.perform(post("/personnel/" + secretaireB.getId() + "/desactivation")
                        .header("Authorization", "Bearer " + jetonAdminA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UTILISATEUR_INTROUVABLE"));

        assertThat(utilisateurRepository.findById(secretaireB.getId()).orElseThrow().isActif()).isTrue();
    }

    @Test
    void onNInvitePasLePersonnelDUnAutreEtablissementNiUnPatient() throws Exception {
        Utilisateur patient = creer(Patient.builder().email("patient.a@test.invalid"), false);

        for (Utilisateur cible : new Utilisateur[] {secretaireB, patient}) {
            mockMvc.perform(post("/personnel/" + cible.getId() + "/invitation")
                            .header("Authorization", "Bearer " + jetonAdminA))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("UTILISATEUR_INTROUVABLE"));
        }
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
    }

    // ---------- Outils ----------

    /** Le rôle est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte, boolean actif) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Personnel")
                .password("non-utilise")
                .actif(actif)
                .build());
    }
}
