package com.forceN.gestionpatient.etablissement;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Gestion des établissements par le super-administrateur. Chaque test est annulé à la fin (@Transactional). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EtablissementGestionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionsDeTest sessions;

    private String jetonSuperAdmin;

    @BeforeEach
    void connecterLeSuperAdministrateur() {
        Utilisateur superAdmin = creer(SuperAdministrateur.builder().email("super.admin.gestion@test.invalid"));
        jetonSuperAdmin = jwtService.genererJeton(superAdmin.getEmail());
    }

    @Test
    void leSuperAdministrateurCreeEtListeUnEtablissement() throws Exception {
        String reponse = mockMvc.perform(post("/etablissements")
                        .header("Authorization", "Bearer " + jetonSuperAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"  Clinique du Plateau ","type":"CLINIQUE","ville":"Dakar",
                                 "email":"Contact@Plateau.SN","adresse":"   "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Clinique du Plateau"))
                .andExpect(jsonPath("$.type").value("CLINIQUE"))
                .andExpect(jsonPath("$.email").value("contact@plateau.sn"))
                .andExpect(jsonPath("$.adresse").value(nullValue())) // une adresse vide n'est pas enregistrée
                .andExpect(jsonPath("$.actif").value(true))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(reponse, "$.id");

        mockMvc.perform(get("/etablissements").header("Authorization", "Bearer " + jetonSuperAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
    }

    @Test
    void unEtablissementSansNomNiTypeDonne400() throws Exception {
        mockMvc.perform(post("/etablissements")
                        .header("Authorization", "Bearer " + jetonSuperAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ville\":\"Thiès\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"))
                .andExpect(jsonPath("$.erreurs.nom").exists())
                .andExpect(jsonPath("$.erreurs.type").exists());
    }

    @Test
    void desactiverUnEtablissementCoupeSonPersonnelEtLeReactiverLeRend() throws Exception {
        Etablissement clinique = sessions.creerClinique("Clinique A");
        String jetonAdmin = sessions.jeton(creer(Administrateur.builder().email("admin.gestion@test.invalid")), clinique);
        travailler(jetonAdmin).andExpect(status().isOk());

        changerEtat(clinique.getId(), "desactivation").andExpect(status().isNoContent());
        travailler(jetonAdmin)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCES_ETABLISSEMENT_RETIRE"));

        changerEtat(clinique.getId(), "reactivation").andExpect(status().isNoContent());
        travailler(jetonAdmin).andExpect(status().isOk());
    }

    @Test
    void unEtablissementInconnuDonne404() throws Exception {
        changerEtat(999_999_999, "desactivation")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_INTROUVABLE"));
    }

    @Test
    void niLAdministrateurNiLePatientNeGerentLesEtablissements() throws Exception {
        Etablissement clinique = sessions.creerClinique("Clinique A");
        String jetonAdmin = sessions.jeton(creer(Administrateur.builder().email("admin.gestion@test.invalid")), clinique);
        String jetonPatient = sessions.jeton(creer(Patient.builder().email("patient.gestion@test.invalid")), clinique);

        for (String jeton : new String[] {jetonAdmin, jetonPatient}) {
            mockMvc.perform(get("/etablissements").header("Authorization", "Bearer " + jeton))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        }
    }

    // ---------- Outils ----------

    /** Le rôle est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Gestion")
                .password("non-utilise")
                .actif(true)
                .build());
    }

    private ResultActions changerEtat(Integer etablissementId, String action) throws Exception {
        return mockMvc.perform(post("/etablissements/" + etablissementId + "/" + action)
                .header("Authorization", "Bearer " + jetonSuperAdmin));
    }

    /** Une route de travail de l'administrateur (permission PERSONNEL_GERER). */
    private ResultActions travailler(String jeton) throws Exception {
        return mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jeton));
    }
}
