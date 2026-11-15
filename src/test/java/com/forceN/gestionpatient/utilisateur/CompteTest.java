package com.forceN.gestionpatient.utilisateur;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.outils.SessionsDeTest;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests des routes de la Partie 1 : mot de passe, profil, gestion du personnel.
 * Chaque test est annulé à la fin (@Transactional) : la base locale n'est pas modifiée.
 * EmailService est remplacé par un faux (@MockitoBean) : aucun email n'est envoyé,
 * et on peut récupérer le jeton qui aurait été mis dans le lien.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompteTest {

    private static final String MOT_DE_PASSE = "ancien-mot-de-passe";
    private static final String EMAIL_PATIENT = "patient.compte@test.invalid";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private SessionsDeTest sessions;

    @Value("${securite.jwt.secret}")
    private String secretJwt;

    // ---------- Changer son mot de passe ----------

    @Test
    void changerSonMotDePasseFermeLesAnciennesSessions() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));
        // Jeton créé quelques secondes avant le changement, comme une session ouverte plus tôt.
        String ancienJeton = jetonEmisIlYA(EMAIL_PATIENT, 5);

        changerMotDePasse(ancienJeton, MOT_DE_PASSE, "nouveau-mot-de-passe")
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/utilisateurs/moi").header("Authorization", "Bearer " + ancienJeton))
                .andExpect(status().isUnauthorized());
        connecter(EMAIL_PATIENT, MOT_DE_PASSE).andExpect(status().isUnauthorized());
        connecter(EMAIL_PATIENT, "nouveau-mot-de-passe").andExpect(status().isOk());
    }

    @Test
    void ancienMotDePasseFauxDonne400() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));

        changerMotDePasse(jwtService.genererJeton(EMAIL_PATIENT), "mauvais-mot-de-passe", "nouveau-mot-de-passe")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MOT_DE_PASSE_INCORRECT"));
    }

    // ---------- Mot de passe oublié ----------

    @Test
    void motDePasseOublieAvecUnEmailInconnuNEnvoieRien() throws Exception {
        demanderReinitialisation("personne@test.invalid").andExpect(status().isNoContent());

        verify(emailService, never()).envoyerReinitialisation(anyString(), anyString(), anyString());
    }

    @Test
    void motDePasseOublieEnvoieUnLienQuiNeSertQuUneFois() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));

        demanderReinitialisation(EMAIL_PATIENT.toUpperCase()).andExpect(status().isNoContent());

        // Récupère le jeton que l'email aurait contenu.
        ArgumentCaptor<String> jeton = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyerReinitialisation(eq(EMAIL_PATIENT), any(), jeton.capture());

        reinitialiser(jeton.getValue(), "choisi-apres-oubli").andExpect(status().isNoContent());
        reinitialiser(jeton.getValue(), "autre-mot-de-passe")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JETON_INVALIDE"));
        connecter(EMAIL_PATIENT, "choisi-apres-oubli").andExpect(status().isOk());
    }

    // ---------- Profil ----------

    @Test
    void lePatientModifieSonProfilMaisPasSonEmail() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));

        mockMvc.perform(put("/utilisateurs/moi")
                        .header("Authorization", "Bearer " + jwtService.genererJeton(EMAIL_PATIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":" Diop ","prenom":"Awa","telephone":"781234567","email":"autre@test.invalid"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Diop"))
                .andExpect(jsonPath("$.telephone").value("781234567"))
                .andExpect(jsonPath("$.email").value(EMAIL_PATIENT));
    }

    // ---------- Gestion du personnel ----------

    @Test
    void unPatientNePeutPasListerLePersonnel() throws Exception {
        creer(Patient.builder().email(EMAIL_PATIENT));

        mockMvc.perform(get("/personnel").header("Authorization", "Bearer " + jwtService.genererJeton(EMAIL_PATIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unMembreSuspenduPerdSonAccesALEtablissementToutDeSuite() throws Exception {
        String jetonAdmin = jetonAdministrateur();
        Utilisateur secretaire = creer(Secretaire.builder().email("secretaire.compte@test.invalid"));
        // La secrétaire travaille dans la même clinique que l'administrateur.
        String jetonSecretaire = sessions.jeton(secretaire, clinique);

        mockMvc.perform(post("/personnel/" + secretaire.getId() + "/desactivation")
                        .header("Authorization", "Bearer " + jetonAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/utilisateurs/moi").header("Authorization", "Bearer " + jetonSecretaire))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCES_ETABLISSEMENT_RETIRE"));
    }

    @Test
    void unNumeroOrdreDejaPrisDonne409() throws Exception {
        String jetonAdmin = jetonAdministrateur();
        creer(Medecin.builder().email("medecin.compte@test.invalid").specialite("Cardiologie").numeroOrdre("TEST-ORDRE-1"));

        mockMvc.perform(post("/personnel")
                        .header("Authorization", "Bearer " + jetonAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Fall","prenom":"Moussa","email":"autre.medecin@test.invalid",
                                 "role":"MEDECIN","specialite":"Pédiatrie","numeroOrdre":"TEST-ORDRE-1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NUMERO_ORDRE_DEJA_UTILISE"));
        verify(emailService, never()).envoyerInvitation(anyString(), anyString(), anyString());
    }

    // ---------- Outils ----------

    // La clinique de l'administrateur de test (créée par jetonAdministrateur).
    private Etablissement clinique;

    /** Crée un administrateur affilié à une clinique de test, et le jeton qu'il recevrait en se connectant. */
    private String jetonAdministrateur() {
        Utilisateur admin = creer(Administrateur.builder().email("admin.compte@test.invalid"));
        clinique = sessions.creerClinique("Clinique Compte Test");
        return sessions.jeton(admin, clinique);
    }

    /** Enregistre un compte actif avec le mot de passe MOT_DE_PASSE. Le rôle est fixé par @PrePersist. */
    private Utilisateur creer(Utilisateur.UtilisateurBuilder<?, ?> compte) {
        return utilisateurRepository.saveAndFlush(compte
                .nom("Test")
                .prenom("Compte")
                .password(passwordEncoder.encode(MOT_DE_PASSE))
                .actif(true)
                .build());
    }

    /** Fabrique un jeton de connexion valide, mais créé il y a quelques secondes. */
    private String jetonEmisIlYA(String email, long secondes) {
        Instant emisLe = Instant.now().minusSeconds(secondes);
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(emisLe))
                .expiration(Date.from(emisLe.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(secretJwt.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private ResultActions connecter(String email, String motDePasse) throws Exception {
        return mockMvc.perform(post("/auth/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}"));
    }

    private ResultActions changerMotDePasse(String jeton, String ancien, String nouveau) throws Exception {
        return mockMvc.perform(put("/utilisateurs/moi/mot-de-passe")
                .header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ancienMotDePasse\":\"" + ancien + "\",\"nouveauMotDePasse\":\"" + nouveau + "\"}"));
    }

    private ResultActions demanderReinitialisation(String email) throws Exception {
        return mockMvc.perform(post("/auth/mot-de-passe-oublie")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions reinitialiser(String jeton, String motDePasse) throws Exception {
        return mockMvc.perform(post("/auth/reinitialisation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jeton\":\"" + jeton + "\",\"motDePasse\":\"" + motDePasse + "\"}"));
    }
}
