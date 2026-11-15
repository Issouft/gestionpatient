package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.config.SuperAdministrateurInitial;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Le super-administrateur créé au démarrage. Chaque test est annulé à la fin (@Transactional). */
@SpringBootTest
@Transactional
class SuperAdministrateurInitialTest {

    private static final String EMAIL = "super.admin@test.invalid";

    @Autowired
    private SuperAdministrateurInitial superAdministrateurInitial;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AffiliationRepository affiliationRepository;

    // Aucun email ne doit partir pendant les tests.
    @MockitoBean
    private EmailService emailService;

    @Test
    void leSuperAdministrateurEstCreeInactifSansEtablissement() {
        // Il n'est créé que s'il n'en existe aucun : ce test ne vaut que sur une base sans super-administrateur
        // (une base neuve, comme celle des tests automatiques). Sinon, il est ignoré.
        assumeThat(utilisateurRepository.findFirstByRole(Role.SUPER_ADMINISTRATEUR)).isEmpty();

        demarrerAvec(EMAIL);

        Utilisateur superAdmin = utilisateurRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(superAdmin).isInstanceOf(SuperAdministrateur.class);
        assertThat(superAdmin.getRole()).isEqualTo(Role.SUPER_ADMINISTRATEUR);
        assertThat(superAdmin.isActif()).isFalse(); // il choisira son mot de passe avec le lien
        assertThat(affiliationRepository.findByUtilisateurIdAndStatut(superAdmin.getId(), StatutAffiliation.ACTIVE))
                .isEmpty();
        verify(emailService).envoyerInvitation(eq(EMAIL), anyString(), anyString());
    }

    @Test
    void unEmailDejaPrisNeFaitPasPlanterLeDemarrage() {
        assumeThat(utilisateurRepository.findFirstByRole(Role.SUPER_ADMINISTRATEUR)).isEmpty();
        utilisateurRepository.saveAndFlush(Patient.builder()
                .nom("Test").prenom("Patient").email(EMAIL).password("non-utilise").actif(true).build());

        demarrerAvec(EMAIL); // ne lance pas d'exception : un message d'erreur clair est écrit dans la console

        assertThat(utilisateurRepository.findFirstByRole(Role.SUPER_ADMINISTRATEUR)).isEmpty();
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
    }

    private void demarrerAvec(String email) {
        ReflectionTestUtils.setField(superAdministrateurInitial, "email", email);
        try {
            superAdministrateurInitial.run();
        } finally {
            ReflectionTestUtils.setField(superAdministrateurInitial, "email", "");
        }
    }
}
