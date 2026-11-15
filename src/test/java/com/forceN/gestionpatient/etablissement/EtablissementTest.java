package com.forceN.gestionpatient.etablissement;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Vérifie les entités Etablissement et Affiliation avec les tables créées par les migrations V2 et V3. */
@SpringBootTest
@Transactional // chaque test est annulé à la fin : la base n'est pas modifiée
class EtablissementTest {

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private AffiliationRepository affiliationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void unEtablissementEstEnregistreEtActifParDefaut() {
        Etablissement clinique = creerClinique("Clinique Test");

        Etablissement relue = etablissementRepository.findById(clinique.getId()).orElseThrow();
        assertThat(relue.getNom()).isEqualTo("Clinique Test");
        assertThat(relue.getType()).isEqualTo(TypeEtablissement.CLINIQUE);
        assertThat(relue.isActif()).isTrue();
        assertThat(relue.getCreationDate()).isNotNull();
    }

    @Test
    void unMedecinPeutTravaillerDansDeuxCliniques() {
        Utilisateur medecin = creerMedecin();
        Etablissement cliniqueA = creerClinique("Clinique A");
        Etablissement cliniqueB = creerClinique("Clinique B");

        affilier(medecin, cliniqueA, Role.MEDECIN);
        affilier(medecin, cliniqueB, Role.MEDECIN);

        assertThat(affiliationRepository.findByUtilisateurIdAndStatut(medecin.getId(), StatutAffiliation.ACTIVE))
                .extracting(affiliation -> affiliation.getEtablissement().getNom())
                .containsExactlyInAnyOrder("Clinique A", "Clinique B");
    }

    @Test
    void leMemeRoleDansLeMemeEtablissementEstRefuse() {
        Utilisateur medecin = creerMedecin();
        Etablissement clinique = creerClinique("Clinique A");
        affilier(medecin, clinique, Role.MEDECIN);

        assertThat(affiliationRepository.existsByUtilisateurIdAndEtablissementIdAndRole(
                medecin.getId(), clinique.getId(), Role.MEDECIN)).isTrue();
        // Même sans vérification dans le code, la base refuse le doublon.
        assertThatThrownBy(() -> affilier(medecin, clinique, Role.MEDECIN))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uneAffiliationSuspendueNeComptePlusParmiLesActives() {
        Utilisateur medecin = creerMedecin();
        Affiliation affiliation = affilier(medecin, creerClinique("Clinique A"), Role.MEDECIN);

        affiliation.setStatut(StatutAffiliation.SUSPENDUE);
        affiliationRepository.saveAndFlush(affiliation);

        assertThat(affiliation.estActive()).isFalse();
        assertThat(affiliationRepository.findByUtilisateurIdAndStatut(medecin.getId(), StatutAffiliation.ACTIVE))
                .isEmpty();
    }

    // ---------- Outils ----------

    private Etablissement creerClinique(String nom) {
        return etablissementRepository.saveAndFlush(Etablissement.builder()
                .nom(nom)
                .type(TypeEtablissement.CLINIQUE)
                .ville("Thiès")
                .build());
    }

    /** Le rôle MEDECIN est fixé par @PrePersist. Le mot de passe n'est pas utilisé ici. */
    private Utilisateur creerMedecin() {
        return utilisateurRepository.saveAndFlush(Medecin.builder()
                .nom("Test")
                .prenom("Affiliation")
                .email("medecin.affiliation@test.invalid")
                .password("non-utilise")
                .specialite("Cardiologie")
                .numeroOrdre("TEST-AFFILIATION-1")
                .build());
    }

    private Affiliation affilier(Utilisateur utilisateur, Etablissement etablissement, Role role) {
        return affiliationRepository.saveAndFlush(Affiliation.builder()
                .utilisateur(utilisateur)
                .etablissement(etablissement)
                .role(role)
                .build());
    }
}
