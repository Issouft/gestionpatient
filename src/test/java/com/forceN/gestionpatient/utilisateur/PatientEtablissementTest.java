package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le patient rejoint des cliniques : à l'inscription ou plus tard, et seulement des cliniques actives.
 * Vérifie aussi la règle de confidentialité estPatientDe, que les Parties 2 et 3 utiliseront.
 * Chaque test est annulé à la fin (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientEtablissementTest {

    private static final String EMAIL = "patient.clinique@test.invalid";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AffiliationService affiliationService;

    @Autowired
    private SessionsDeTest sessions;

    private Etablissement cliniqueA;
    private Etablissement cliniqueB;

    @BeforeEach
    void creerLesCliniques() {
        cliniqueA = sessions.creerClinique("Clinique A Patient");
        cliniqueB = sessions.creerClinique("Clinique B Patient");
    }

    // ---------- À l'inscription ----------

    @Test
    void lePatientRejointLaCliniqueChoisieDesLInscription() throws Exception {
        inscrire(cliniqueA.getId()).andExpect(status().isCreated());

        Utilisateur patient = utilisateurRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(affiliationService.estPatientDe(patient.getId(), cliniqueA.getId())).isTrue();
        assertThat(affiliationService.estPatientDe(patient.getId(), cliniqueB.getId())).isFalse();
        mesCliniques(jeton(patient))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(cliniqueA.getId()));
    }

    @Test
    void sansCliniqueLInscriptionMarcheCommeAvant() throws Exception {
        inscrire(null).andExpect(status().isCreated());

        mesCliniques(jeton(utilisateurRepository.findByEmail(EMAIL).orElseThrow()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void uneCliniqueInconnueOuDesactiveeDonne404EtAucunCompteNEstCree() throws Exception {
        cliniqueB.setActif(false);
        for (Integer id : new Integer[] {cliniqueB.getId(), 999_999_999}) {
            inscrire(id)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ETABLISSEMENT_INTROUVABLE"));
        }
        assertThat(utilisateurRepository.existsByEmail(EMAIL)).isFalse();
    }

    // ---------- Après l'inscription ----------

    @Test
    void lePatientRejointPlusieursCliniquesEtLesRetrouveTrieesParNom() throws Exception {
        String jeton = jeton(patient());

        rejoindre(jeton, cliniqueB.getId()).andExpect(status().isNoContent());
        rejoindre(jeton, cliniqueA.getId()).andExpect(status().isNoContent());
        rejoindre(jeton, cliniqueA.getId()).andExpect(status().isNoContent()); // déjà fait : rien ne change

        mesCliniques(jeton)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("Clinique A Patient"))
                .andExpect(jsonPath("$[1].nom").value("Clinique B Patient"));
    }

    @Test
    void unPatientSuspenduNePeutPasSeReinscrireSeul() throws Exception {
        Utilisateur patient = patient();
        sessions.affilier(patient, cliniqueA).setStatut(StatutAffiliation.SUSPENDUE);

        rejoindre(jeton(patient), cliniqueA.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADHESION_SUSPENDUE"));
        assertThat(affiliationService.estPatientDe(patient.getId(), cliniqueA.getId())).isFalse();
        mesCliniques(jeton(patient)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void uneCliniqueDesactiveeDisparaitDesCliniquesDuPatient() throws Exception {
        Utilisateur patient = patient();
        String jeton = jeton(patient);
        rejoindre(jeton, cliniqueA.getId()).andExpect(status().isNoContent());

        cliniqueA.setActif(false);

        mesCliniques(jeton).andExpect(jsonPath("$", hasSize(0)));
        assertThat(affiliationService.estPatientDe(patient.getId(), cliniqueA.getId())).isFalse();
        rejoindre(jeton, cliniqueA.getId())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETABLISSEMENT_INTROUVABLE"));
    }

    @Test
    void seulLePatientPeutRejoindreUneClinique() throws Exception {
        String jetonMedecin = sessions.jeton(creer(Medecin.builder().email("medecin.clinique@test.invalid")
                .specialite("Cardiologie").numeroOrdre("ORD-PATIENT-CLINIQUE")), cliniqueA);
        String jetonAdmin = sessions.jeton(creer(Administrateur.builder().email("admin.clinique@test.invalid")), cliniqueA);
        String jetonSuperAdmin = jeton(creer(SuperAdministrateur.builder().email("super.clinique@test.invalid")));

        for (String jeton : new String[] {jetonMedecin, jetonAdmin, jetonSuperAdmin}) {
            rejoindre(jeton, cliniqueB.getId())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
        }
        mockMvc.perform(post("/patients/moi/etablissements/" + cliniqueB.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Outils ----------

    private ResultActions inscrire(Integer etablissementId) throws Exception {
        String clinique = etablissementId == null ? "" : ",\"etablissementId\":" + etablissementId;
        return mockMvc.perform(post("/auth/inscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nom":"Sarr","prenom":"Aminata","email":"%s","motDePasse":"mot-de-passe-patient",
                         "dateNaissance":"1990-05-12","sexe":"FEMININ","telephone":"770000000","adresse":"Thiès"%s}
                        """.formatted(EMAIL, clinique)));
    }

    private ResultActions rejoindre(String jeton, Integer etablissementId) throws Exception {
        return mockMvc.perform(post("/patients/moi/etablissements/" + etablissementId)
                .header("Authorization", "Bearer " + jeton));
    }

    private ResultActions mesCliniques(String jeton) throws Exception {
        return mockMvc.perform(get("/patients/moi/etablissements").header("Authorization", "Bearer " + jeton));
    }

    /** Le jeton d'une personne qui agit sans établissement (patient, super-administrateur). */
    private String jeton(Utilisateur utilisateur) {
        return jwtService.genererJeton(utilisateur.getEmail());
    }

    private Utilisateur patient() {
        return creer(Patient.builder().email(EMAIL));
    }

    /** Le rôle est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Clinique")
                .password("non-utilise")
                .actif(true)
                .build());
    }
}
