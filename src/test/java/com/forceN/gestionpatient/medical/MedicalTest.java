package com.forceN.gestionpatient.medical;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.medical.entities.DossierPatient;
import com.forceN.gestionpatient.medical.entities.Medicament;
import com.forceN.gestionpatient.medical.repository.DossierPatientRepository;
import com.forceN.gestionpatient.medical.repository.MedicamentRepository;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.rendezvous.entities.RendezVous;
import com.forceN.gestionpatient.rendezvous.enums.StatutRendezVous;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests des routes de la Partie 3 (medical) : dossier, consultation, ordonnance, médicaments, demandes, rapports.
 * Chaque test est annulé à la fin (@Transactional) : la base locale n'est pas modifiée.
 * La Partie 2 n'ayant pas encore de routes, les rendez-vous sont créés directement en base.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MedicalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DossierPatientRepository dossierPatientRepository;

    @Autowired
    private MedicamentRepository medicamentRepository;

    @Autowired
    private EntityManager entityManager;

    // Aucun email ne doit partir pendant les tests.
    @MockitoBean
    private EmailService emailService;

    private Patient patient;
    private Secretaire secretaire;
    private Medecin medecin;
    private Pharmacien pharmacien;

    // Le personnel travaille dans cette clinique : son jeton la désigne (voir envoyer()).
    @Autowired
    private SessionsDeTest sessions;

    private Etablissement clinique;

    @BeforeEach
    void creerLesComptes() {
        clinique = sessions.creerClinique("Clinique Medical Test");
        patient = creer(Patient.builder().email("patient.medical@test.invalid"));
        secretaire = creer(Secretaire.builder().email("secretaire.medical@test.invalid"));
        medecin = creer(Medecin.builder().email("medecin.medical@test.invalid")
                .specialite("Généraliste").numeroOrdre("TEST-MEDICAL-1"));
        pharmacien = creer(Pharmacien.builder().email("pharmacien.medical@test.invalid"));
    }

    // ---------- Dossier patient ----------

    @Test
    void laSecretaireCreeLeDossierUneSeuleFois() throws Exception {
        String corps = "{\"patientId\":" + patient.getId() + ",\"antecedents\":\" Asthme \"}";

        envoyer(post("/dossiers"), secretaire, corps)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patient.getId()))
                .andExpect(jsonPath("$.antecedents").value("Asthme"))
                .andExpect(jsonPath("$.creeParId").value(secretaire.getId()));

        envoyer(post("/dossiers"), secretaire, corps)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DOSSIER_DEJA_EXISTANT"));
    }

    @Test
    void unPatientNePeutPasCreerDeDossier() throws Exception {
        envoyer(post("/dossiers"), patient, "{\"patientId\":" + patient.getId() + "}")
                .andExpect(status().isForbidden());
    }

    @Test
    void unDossierPourUnCompteQuiNEstPasPatientDonne404() throws Exception {
        envoyer(post("/dossiers"), secretaire, "{\"patientId\":" + medecin.getId() + "}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATIENT_INTROUVABLE"));
    }

    // ---------- Consultation ----------

    @Test
    void leMedecinConsulteEtLaConsultationSAjouteAuDossier() throws Exception {
        DossierPatient dossier = creerDossier();
        RendezVous rendezVous = creerRendezVous(medecin, StatutRendezVous.CONFIRME);

        envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dossierPatientId").value(dossier.getId()))
                .andExpect(jsonPath("$.medecinId").value(medecin.getId()))
                .andExpect(jsonPath("$.diagnostic").value("Grippe"));

        assertThat(rendezVous.getStatut()).isEqualTo(StatutRendezVous.TERMINE);
        lire(get("/dossiers/patient/" + patient.getId()), medecin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultations.length()").value(1));
    }

    @Test
    void unRendezVousNeDonneQuUneConsultation() throws Exception {
        creerDossier();
        RendezVous rendezVous = creerRendezVous(medecin, StatutRendezVous.CONFIRME);
        envoyer(post("/consultations"), medecin, consultation(rendezVous)).andExpect(status().isCreated());

        envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONSULTATION_DEJA_EXISTANTE"));
    }

    @Test
    void unMedecinNeConsultePasLeRendezVousDUnAutre() throws Exception {
        creerDossier();
        Medecin autre = creer(Medecin.builder().email("autre.medecin.medical@test.invalid")
                .specialite("Pédiatrie").numeroOrdre("TEST-MEDICAL-2"));
        RendezVous rendezVous = creerRendezVous(autre, StatutRendezVous.CONFIRME);

        envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTRE_MEDECIN"));
    }

    @Test
    void unRendezVousNonConfirmeNeDonnePasDeConsultation() throws Exception {
        creerDossier();
        RendezVous rendezVous = creerRendezVous(medecin, StatutRendezVous.EN_ATTENTE);

        envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RENDEZ_VOUS_NON_CONFIRME"));
    }

    @Test
    void sansDossierLaConsultationEstRefusee() throws Exception {
        RendezVous rendezVous = creerRendezVous(medecin, StatutRendezVous.CONFIRME);

        envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOSSIER_INTROUVABLE"));
    }

    // ---------- Ordonnance ----------

    @Test
    void leMedecinRedigeUneSeuleOrdonnanceParConsultation() throws Exception {
        Integer consultationId = creerConsultation();
        Medicament paracetamol = creerMedicament("Paracétamol", 10);
        String corps = ordonnance(paracetamol.getId());

        envoyer(post("/consultations/" + consultationId + "/ordonnance"), medecin, corps)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consultationId").value(consultationId))
                .andExpect(jsonPath("$.lignes[0].medicamentNom").value("Paracétamol"))
                .andExpect(jsonPath("$.lignes[0].quantite").value(2));
        // Prescrire ne touche pas au stock.
        assertThat(paracetamol.getStock()).isEqualTo(10);

        envoyer(post("/consultations/" + consultationId + "/ordonnance"), medecin, corps)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDONNANCE_DEJA_EXISTANTE"));
        lire(get("/consultations/" + consultationId + "/ordonnance"), pharmacien)
                .andExpect(status().isOk());
    }

    @Test
    void uneOrdonnanceSansMedicamentEstRefusee() throws Exception {
        Integer consultationId = creerConsultation();

        envoyer(post("/consultations/" + consultationId + "/ordonnance"), medecin, "{\"lignes\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"));
    }

    @Test
    void unAutreMedecinNePeutPasRedigerLOrdonnance() throws Exception {
        Integer consultationId = creerConsultation();
        Medecin autre = creer(Medecin.builder().email("autre.medecin.medical@test.invalid")
                .specialite("Pédiatrie").numeroOrdre("TEST-MEDICAL-2"));

        envoyer(post("/consultations/" + consultationId + "/ordonnance"), autre,
                ordonnance(creerMedicament("Paracétamol", 10).getId()))
                .andExpect(status().isForbidden());
    }

    // ---------- Médicaments ----------

    @Test
    void lePharmacienAjouteUnMedicamentEtLePatientLeVoit() throws Exception {
        envoyer(post("/medicaments"), pharmacien,
                "{\"nom\":\"Amoxicilline\",\"dosage\":\"500 mg\",\"stock\":3,\"seuilAlerte\":5}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.sousSeuilAlerte").value(true));

        lire(get("/medicaments"), patient)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nom == 'Amoxicilline')]").exists());
    }

    @Test
    void unPatientNePeutPasAjouterDeMedicament() throws Exception {
        envoyer(post("/medicaments"), patient, "{\"nom\":\"X\",\"dosage\":\"1 mg\",\"stock\":1,\"seuilAlerte\":0}")
                .andExpect(status().isForbidden());
    }

    // ---------- Demandes de disponibilité ----------

    @Test
    void leStockBaisseQuandLePharmacienConfirmeLeRetrait() throws Exception {
        Medicament medicament = creerMedicament("Ibuprofène", 10);
        Integer demandeId = creerDemande(medicament, 3);

        // Validée : le stock ne bouge pas encore.
        envoyer(post("/demandes/" + demandeId + "/validation"), pharmacien, "{\"reponse\":\"Passez demain\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"))
                .andExpect(jsonPath("$.reponse").value("Passez demain"));
        assertThat(medicament.getStock()).isEqualTo(10);

        // Retrait confirmé : le stock baisse de la quantité demandée, une seule fois.
        envoyer(post("/demandes/" + demandeId + "/retrait"), pharmacien, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateRetrait").exists());
        assertThat(medicament.getStock()).isEqualTo(7);

        envoyer(post("/demandes/" + demandeId + "/retrait"), pharmacien, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEMANDE_DEJA_RETIREE"));
        assertThat(medicament.getStock()).isEqualTo(7);

        lire(get("/demandes/moi"), patient)
                .andExpect(jsonPath("$[0].statut").value("VALIDEE"))
                .andExpect(jsonPath("$[0].reponse").value("Passez demain"));
    }

    @Test
    void onNeRetirePasUneDemandeNonValidee() throws Exception {
        Integer demandeId = creerDemande(creerMedicament("Ibuprofène", 10), 1);

        envoyer(post("/demandes/" + demandeId + "/retrait"), pharmacien, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEMANDE_NON_VALIDEE"));
    }

    @Test
    void onNeValidePasSiLeStockNeSuffitPas() throws Exception {
        Integer demandeId = creerDemande(creerMedicament("Ibuprofène", 2), 5);

        envoyer(post("/demandes/" + demandeId + "/validation"), pharmacien, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOCK_INSUFFISANT"));
    }

    @Test
    void uneDemandeRefuseeNePeutPlusEtreValidee() throws Exception {
        Integer demandeId = creerDemande(creerMedicament("Ibuprofène", 10), 1);

        envoyer(post("/demandes/" + demandeId + "/refus"), pharmacien, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSEE"))
                .andExpect(jsonPath("$.reponse").exists());

        envoyer(post("/demandes/" + demandeId + "/validation"), pharmacien, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEMANDE_DEJA_TRAITEE"));
    }

    // ---------- Rapports ----------

    @Test
    void lAdministrateurGenereUnRapportDActivite() throws Exception {
        Administrateur admin = creer(Administrateur.builder().email("admin.medical@test.invalid"));
        creerConsultation();
        String aujourdHui = java.time.LocalDate.now().toString();

        envoyer(post("/rapports"), admin,
                "{\"type\":\"ACTIVITE_MEDICALE\",\"periodeDebut\":\"" + aujourdHui + "\",\"periodeFin\":\"" + aujourdHui + "\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.administrateurId").value(admin.getId()))
                .andExpect(jsonPath("$.periodeDebut").value(aujourdHui))
                .andExpect(jsonPath("$.resume", containsString("consultation(s)")));
    }

    @Test
    void uneFinAvantLeDebutDonne400() throws Exception {
        Administrateur admin = creer(Administrateur.builder().email("admin.medical@test.invalid"));

        envoyer(post("/rapports"), admin,
                "{\"type\":\"STOCK_MEDICAMENTS\",\"periodeDebut\":\"2026-11-30\",\"periodeFin\":\"2026-11-01\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PERIODE_INVALIDE"));
    }

    @Test
    void unMedecinNePeutPasGenererDeRapport() throws Exception {
        envoyer(post("/rapports"), medecin,
                "{\"type\":\"RENDEZ_VOUS\",\"periodeDebut\":\"2026-11-01\",\"periodeFin\":\"2026-11-30\"}")
                .andExpect(status().isForbidden());
    }

    // ---------- Outils ----------

    /** Enregistre un compte actif. Le rôle est fixé par @PrePersist. */
    private <T extends Utilisateur> T creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        @SuppressWarnings("unchecked")
        T enregistre = (T) utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Medical")
                .password("inutile-ici")
                .actif(true)
                .build());
        return enregistre;
    }

    private DossierPatient creerDossier() {
        return dossierPatientRepository.saveAndFlush(DossierPatient.builder().patient(patient).creePar(secretaire).build());
    }

    // La Partie 2 n'a pas encore de repository : le rendez-vous est enregistré directement.
    private RendezVous creerRendezVous(Medecin medecinDuRendezVous, StatutRendezVous statut) {
        RendezVous rendezVous = RendezVous.builder()
                .patient(patient)
                .medecin(medecinDuRendezVous)
                .dateHeure(LocalDateTime.now())
                .motif("Fièvre")
                .statut(statut)
                .build();
        entityManager.persist(rendezVous);
        entityManager.flush();
        return rendezVous;
    }

    private Medicament creerMedicament(String nom, int stock) {
        return medicamentRepository.saveAndFlush(Medicament.builder()
                .nom(nom).dosage("500 mg").stock(stock).seuilAlerte(1).pharmacien(pharmacien).build());
    }

    /** Dossier + rendez-vous confirmé + consultation par l'API. Renvoie l'id de la consultation. */
    private Integer creerConsultation() throws Exception {
        creerDossier();
        RendezVous rendezVous = creerRendezVous(medecin, StatutRendezVous.CONFIRME);
        String reponse = envoyer(post("/consultations"), medecin, consultation(rendezVous))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(reponse, "$.id");
    }

    private Integer creerDemande(Medicament medicament, int quantite) throws Exception {
        String reponse = envoyer(post("/demandes"), patient,
                "{\"medicamentId\":" + medicament.getId() + ",\"quantite\":" + quantite + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(reponse, "$.id");
    }

    private String consultation(RendezVous rendezVous) {
        return "{\"rendezVousId\":" + rendezVous.getId() + ",\"observations\":\"Fièvre depuis 2 jours\",\"diagnostic\":\"Grippe\"}";
    }

    private String ordonnance(Integer medicamentId) {
        return "{\"lignes\":[{\"medicamentId\":" + medicamentId
                + ",\"quantite\":2,\"posologie\":\"1 comprimé matin et soir\",\"dureeJours\":5}]}";
    }

    private ResultActions envoyer(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requete,
            Utilisateur connecte, String corps) throws Exception {
        requete.header("Authorization", "Bearer " + sessions.jeton(connecte, clinique));
        if (corps != null) {
            requete.contentType(MediaType.APPLICATION_JSON).content(corps);
        }
        return mockMvc.perform(requete);
    }

    private ResultActions lire(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requete,
            Utilisateur connecte) throws Exception {
        return envoyer(requete, connecte, null);
    }
}
