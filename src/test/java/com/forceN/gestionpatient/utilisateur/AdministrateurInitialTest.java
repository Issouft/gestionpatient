package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.config.AdministrateurInitial;
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
import static org.mockito.Mockito.verify;

/** Le premier administrateur, créé au démarrage, est rattaché à un établissement. Annulé à la fin (@Transactional). */
@SpringBootTest
@Transactional
class AdministrateurInitialTest {

    private static final String EMAIL = "premier.admin@test.invalid";

    @Autowired
    private AdministrateurInitial administrateurInitial;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AffiliationRepository affiliationRepository;

    // Aucun email ne doit partir pendant les tests.
    @MockitoBean
    private EmailService emailService;

    @Test
    void lePremierAdministrateurEstRattacheAUnEtablissement() {
        // Le démarrage ne crée un administrateur que s'il n'en existe aucun : ce test ne vaut que sur une base sans administrateur
        // (une base neuve, comme celle des tests automatiques). Sur une base qui en a déjà un, il est ignoré.
        assumeThat(utilisateurRepository.findFirstByRole(Role.ADMINISTRATEUR)).isEmpty();
        ReflectionTestUtils.setField(administrateurInitial, "email", EMAIL);
        try {
            administrateurInitial.run();
        } finally {
            ReflectionTestUtils.setField(administrateurInitial, "email", "");
        }

        Utilisateur admin = utilisateurRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(admin.isActif()).isFalse(); // il choisira son mot de passe avec le lien
        assertThat(affiliationRepository.findByUtilisateurIdAndStatut(admin.getId(), StatutAffiliation.ACTIVE))
                .singleElement()
                .satisfies(affiliation -> assertThat(affiliation.getRole()).isEqualTo(Role.ADMINISTRATEUR));
        verify(emailService).envoyerInvitation(eq(EMAIL), anyString(), anyString());
    }
}
