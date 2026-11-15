package com.forceN.gestionpatient.etablissement;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * L'annuaire public : consultable sans jeton, il ne montre que ce qui est actif, et seulement les fiches publiques.
 * Les villes de test n'existent pas, pour ne pas mélanger avec les établissements de la base locale.
 * Chaque test est annulé à la fin (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnnuaireTest {

    private static final String VILLE = "Villetest-Annuaire";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private SessionsDeTest sessions;

    private Etablissement clinique;

    @BeforeEach
    void creerLaClinique() {
        clinique = etablir("Clinique Annuaire", TypeEtablissement.CLINIQUE, VILLE, true);
    }

    // ---------- Établissements ----------

    @Test
    void lAnnuaireMontreLesEtablissementsActifsSansJeton() throws Exception {
        etablir("Cabinet Annuaire", TypeEtablissement.CABINET, VILLE, true);
        etablir("Clinique Fermee", TypeEtablissement.CLINIQUE, VILLE, false);

        // La ville ignore les majuscules et les espaces autour. L'établissement désactivé n'apparaît pas.
        mockMvc.perform(get("/annuaire/etablissements").param("ville", "  VILLETEST-annuaire "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("Cabinet Annuaire"))
                .andExpect(jsonPath("$[1].nom").value("Clinique Annuaire"))
                .andExpect(jsonPath("$[1].email").value("contact@annuaire.test"))
                .andExpect(jsonPath("$[1].actif").doesNotExist());

        mockMvc.perform(get("/annuaire/etablissements").param("ville", VILLE).param("type", "CLINIQUE"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(clinique.getId()));
    }

    @Test
    void unTypeInconnuDonne400AvecLesValeursPossibles() throws Exception {
        mockMvc.perform(get("/annuaire/etablissements").param("type", "HOPITAL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRE_INVALIDE"))
                .andExpect(jsonPath("$.detail", containsString("CLINIQUE, CABINET, PHARMACIE, LABORATOIRE")));
    }

    // ---------- Médecins ----------

    @Test
    void seulsLesMedecinsEnActiviteApparaissentAvecLeurFichePublique() throws Exception {
        Utilisateur cardiologue = medecin("cardio", "Cardiologie", true);
        sessions.affilier(cardiologue, clinique);
        sessions.affilier(medecin("pediatre", "Pédiatrie", true), clinique);
        // Pas affichés : un médecin suspendu, un médecin qui n'a pas activé son compte, une secrétaire.
        sessions.affilier(medecin("suspendu", "Cardiologie", true), clinique).setStatut(StatutAffiliation.SUSPENDUE);
        sessions.affilier(medecin("inactif", "Cardiologie", false), clinique);
        sessions.affilier(compte(Secretaire.builder(), "secretaire", true), clinique);

        mockMvc.perform(get("/annuaire/etablissements/" + clinique.getId() + "/medecins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // La spécialité ignore les majuscules. Ni email ni numéro d'ordre dans la réponse.
        mockMvc.perform(get("/annuaire/etablissements/" + clinique.getId() + "/medecins").param("specialite", "cardiologie"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(cardiologue.getId()))
                .andExpect(jsonPath("$[0].specialite").value("Cardiologie"))
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].numeroOrdre").doesNotExist());
    }

    @Test
    void unEtablissementDesactiveOuInconnuDonneLeMeme404() throws Exception {
        Etablissement fermee = etablir("Clinique Fermee", TypeEtablissement.CLINIQUE, VILLE, false);
        sessions.affilier(medecin("ferme", "Cardiologie", true), fermee);

        for (Integer id : new Integer[] {fermee.getId(), 999_999_999}) {
            mockMvc.perform(get("/annuaire/etablissements/" + id + "/medecins"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ETABLISSEMENT_INTROUVABLE"));
        }
    }

    @Test
    void unJetonInvalideNEmpechePasDeConsulterLAnnuaire() throws Exception {
        mockMvc.perform(get("/annuaire/etablissements").header("Authorization", "Bearer jeton-expire-ou-faux"))
                .andExpect(status().isOk());
    }

    // ---------- Outils ----------

    private Etablissement etablir(String nom, TypeEtablissement type, String ville, boolean actif) {
        return etablissementRepository.saveAndFlush(Etablissement.builder()
                .nom(nom).type(type).ville(ville).email("contact@annuaire.test").actif(actif).build());
    }

    private Utilisateur medecin(String nom, String specialite, boolean actif) {
        return compte(Medecin.builder().specialite(specialite).numeroOrdre("ORD-ANNUAIRE-" + nom), nom, actif);
    }

    /** Le rôle est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur compte(Utilisateur.UtilisateurBuilder<?, ?> builder, String nom, boolean actif) {
        return utilisateurRepository.saveAndFlush(builder
                .nom(nom)
                .prenom("Annuaire")
                .email(nom + ".annuaire@test.invalid")
                .password("non-utilise")
                .actif(actif)
                .build());
    }
}
