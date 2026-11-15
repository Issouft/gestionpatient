package com.forceN.gestionpatient.securite;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Connexion et choix de l'établissement (SaaS).
 * On utilise GET /personnel (permission PERSONNEL_GERER de l'administrateur) comme route « de travail ».
 * Chaque test est annulé à la fin (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConnexionEtablissementTest {

    private static final String MOT_DE_PASSE = "mot-de-passe-de-test";
    private static final String EMAIL_ADMIN = "admin.etablissement@test.invalid";
    private static final String EMAIL_PATIENT = "patient.etablissement@test.invalid";
    private static final String EMAIL_SUPER_ADMIN = "super.admin.etablissement@test.invalid";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private AffiliationRepository affiliationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionsDeTest sessions;

    // ---------- Connexion ----------

    @Test
    void avecUnSeulEtablissementIlEstDirectementDansLeJeton() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        Etablissement clinique = sessions.creerClinique("Clinique A");
        sessions.affilier(admin, clinique);

        String jeton = jetonDe(connecter(EMAIL_ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(clinique.getId()))
                .andExpect(jsonPath("$.etablissements", hasSize(1)))
                .andExpect(jsonPath("$.etablissements[0].etablissementNom").value("Clinique A"))
                .andExpect(jsonPath("$.etablissements[0].roles", contains("ADMINISTRATEUR"))));

        appeler(jeton).andExpect(status().isOk());
    }

    @Test
    void avecPlusieursEtablissementsIlFautChoisir() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        Etablissement cliniqueA = sessions.creerClinique("Clinique A");
        Etablissement cliniqueB = sessions.creerClinique("Clinique B");
        sessions.affilier(admin, cliniqueA);
        sessions.affilier(admin, cliniqueB);

        String jetonSansEtablissement = jetonDe(connecter(EMAIL_ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(nullValue()))
                .andExpect(jsonPath("$.etablissements[*].etablissementNom", contains("Clinique A", "Clinique B"))));

        // Connecté, mais sans établissement : son profil oui, le travail non.
        mockMvc.perform(get("/utilisateurs/moi").header("Authorization", "Bearer " + jetonSansEtablissement))
                .andExpect(status().isOk());
        appeler(jetonSansEtablissement)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_NON_CHOISI"));

        String jetonB = jetonDe(choisir(jetonSansEtablissement, cliniqueB.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(cliniqueB.getId())));
        appeler(jetonB).andExpect(status().isOk());
    }

    @Test
    void lePatientRecoitUnJetonSansEtablissement() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));

        String jeton = jetonDe(connecter(EMAIL_PATIENT)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(nullValue()))
                .andExpect(jsonPath("$.etablissements", hasSize(0))));

        choisir(jeton, sessions.creerClinique("Clinique A").getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_NON_AUTORISE"));
    }

    @Test
    void leSuperAdministrateurNAccedeAAucuneDonneeDesCliniques() throws Exception {
        creer(SuperAdministrateur.builder().email(EMAIL_SUPER_ADMIN));

        String jeton = jetonDe(connecter(EMAIL_SUPER_ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etablissementId").value(nullValue()))
                .andExpect(jsonPath("$.etablissements", hasSize(0))));

        // Décision du groupe : ni le personnel, ni les dossiers, ni les consultations, ni les rapports des cliniques.
        for (String route : new String[] {"/personnel", "/dossiers/patient/1", "/consultations/1", "/rapports",
                "/demandes"}) {
            mockMvc.perform(get(route).header("Authorization", "Bearer " + jeton))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        }
        // Et il ne peut pas se placer dans un établissement.
        choisir(jeton, sessions.creerClinique("Clinique A").getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_NON_AUTORISE"));
    }

    @Test
    void connexionSansEmailDonne401EtPas500() throws Exception {
        mockMvc.perform(post("/auth/connexion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"));
    }

    // ---------- Choix de l'établissement ----------

    @Test
    void unEtablissementInconnuEtUnEtablissementSansAffiliationDonnentLaMemeReponse() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        sessions.affilier(admin, sessions.creerClinique("Clinique A"));
        Etablissement autreClinique = sessions.creerClinique("Clinique sans lui");
        String jeton = jetonDe(connecter(EMAIL_ADMIN));

        String reponseSansAffiliation = choisir(jeton, autreClinique.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_NON_AUTORISE"))
                .andReturn().getResponse().getContentAsString();
        String reponseInconnu = choisir(jeton, 999_999_999)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_NON_AUTORISE"))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(JsonPath.<String>read(reponseInconnu, "$.detail"))
                .isEqualTo(JsonPath.read(reponseSansAffiliation, "$.detail"));
    }

    @Test
    void choisirSansEtablissementDonne400() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        sessions.affilier(admin, sessions.creerClinique("Clinique A"));

        mockMvc.perform(post("/auth/etablissement")
                        .header("Authorization", "Bearer " + jetonDe(connecter(EMAIL_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"));
    }

    // ---------- Accès retiré pendant la session ----------

    @Test
    void uneAffiliationSuspendueCoupeLAccesToutDeSuite() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        Etablissement clinique = sessions.creerClinique("Clinique A");
        Affiliation affiliation = sessions.affilier(admin, clinique);
        String jeton = sessions.jeton(admin, clinique);
        appeler(jeton).andExpect(status().isOk());

        affiliation.setStatut(StatutAffiliation.SUSPENDUE);
        affiliationRepository.saveAndFlush(affiliation);

        appeler(jeton)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCES_ETABLISSEMENT_RETIRE"));
    }

    @Test
    void unEtablissementDesactiveCoupeLAccesToutDeSuite() throws Exception {
        Utilisateur admin = creer(Administrateur.builder().email(EMAIL_ADMIN));
        Etablissement clinique = sessions.creerClinique("Clinique A");
        String jeton = sessions.jeton(admin, clinique);

        clinique.setActif(false);
        etablissementRepository.saveAndFlush(clinique);

        appeler(jeton)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCES_ETABLISSEMENT_RETIRE"));
        // À la connexion suivante, l'établissement désactivé n'est plus proposé.
        connecter(EMAIL_ADMIN)
                .andExpect(jsonPath("$.etablissementId").value(nullValue()))
                .andExpect(jsonPath("$.etablissements", hasSize(0)));
    }

    @Test
    void unJetonDePatientQuiDesigneUnEtablissementEstRefuse() throws Exception {
        Utilisateur patient = creer(Patient.builder().email(EMAIL_PATIENT));
        Etablissement clinique = sessions.creerClinique("Clinique A");

        mockMvc.perform(get("/utilisateurs/moi").header("Authorization",
                        "Bearer " + jwtService.genererJeton(patient.getEmail(), clinique.getId())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    // ---------- Outils ----------

    /** Enregistre un compte actif avec le mot de passe MOT_DE_PASSE. Le rôle est fixé par @PrePersist. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Etablissement")
                .password(passwordEncoder.encode(MOT_DE_PASSE))
                .actif(true)
                .build());
    }

    private ResultActions connecter(String email) throws Exception {
        return mockMvc.perform(post("/auth/connexion").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + MOT_DE_PASSE + "\"}"));
    }

    private ResultActions choisir(String jeton, Integer etablissementId) throws Exception {
        return mockMvc.perform(post("/auth/etablissement")
                .header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etablissementId\":" + etablissementId + "}"));
    }

    /** Une route réservée à la permission PERSONNEL_GERER. */
    private ResultActions appeler(String jeton) throws Exception {
        return mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jeton));
    }

    private String jetonDe(ResultActions resultat) throws Exception {
        return JsonPath.read(resultat.andReturn().getResponse().getContentAsString(), "$.jeton");
    }
}
