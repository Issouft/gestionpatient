package com.forceN.gestionpatient.utilisateur.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.securite.service.JetonActivationService;
import com.forceN.gestionpatient.utilisateur.dto.PersonnelCreationDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.exception.UtilisateurErreur;
import com.forceN.gestionpatient.utilisateur.repository.MedecinRepository;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/**
 * Gestion du personnel (médecin, secrétaire, pharmacien) par l'administrateur : création, invitation, liste,
 * suspension et réactivation.
 * Tout se limite à l'établissement de l'administrateur (celui de son jeton) : il ne voit ni ne touche
 * le personnel des autres établissements.
 */
@Service
@RequiredArgsConstructor
public class PersonnelService {

    // Les rôles que l'administrateur gère ici. Ni patient (il s'inscrit seul), ni administrateur.
    private static final Set<Role> ROLES_PERSONNEL = EnumSet.of(Role.MEDECIN, Role.SECRETAIRE, Role.PHARMACIEN);

    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;
    private final JetonActivationService jetonActivationService;
    private final EmailService emailService;
    private final UtilisateurService utilisateurService;
    private final AffiliationService affiliationService;

    /**
     * Crée un compte inactif pour un membre du personnel et lui envoie le lien d'activation.
     * Il choisira lui-même son mot de passe : l'administrateur ne le connaît jamais.
     */
    // Tout ou rien : si l'email ne part pas, le compte et le jeton sont annulés.
    @Transactional
    public UtilisateurDto creer(PersonnelCreationDto dto, Integer etablissementId) {
        exigerEtablissement(etablissementId);
        // 1. Email normalisé + refus s'il est déjà pris
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.existsByEmail(email)) {
            throw new ApplicationException(UtilisateurErreur.EMAIL_DEJA_UTILISE,
                    "Cet email appartient déjà à un compte");
        }

        // 2. Le bon objet selon le rôle (refuse PATIENT et ADMINISTRATEUR)
        Utilisateur personnel = creerSelonRole(dto);

        // 3. Les champs communs
        personnel.setNom(dto.getNom());
        personnel.setPrenom(dto.getPrenom());
        personnel.setEmail(email);
        // Mot de passe aléatoire que personne ne connaît, en attendant l'activation.
        personnel.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        // Inactif : impossible de se connecter avant d'avoir choisi son mot de passe.
        personnel.setActif(false);

        // 4. Enregistrer, rattacher à l'établissement de l'administrateur, puis créer le jeton et envoyer l'email
        Utilisateur enregistre = utilisateurRepository.save(personnel);
        affiliationService.affilier(enregistre, etablissementId, enregistre.getRole());
        String jeton = jetonActivationService.creerJeton(enregistre);
        emailService.envoyerInvitation(email, enregistre.getPrenom(), jeton);

        // 5. Réponse sans mot de passe
        return utilisateurService.convertiUtilisateurDto(enregistre);
    }

    /**
     * Renvoie un lien d'activation à un membre du personnel qui n'a pas encore activé son compte.
     * L'ancien lien ne marche plus : un seul lien valide à la fois.
     */
    @Transactional
    public void renvoyerInvitation(Integer id, Integer etablissementId) {
        // 1. Retrouver le membre du personnel de cet établissement, sinon 404
        Utilisateur utilisateur = trouverPersonnel(id, etablissementId);

        // 2. Refuser si le compte est déjà actif (409)
        if (utilisateur.isActif()) {
            throw new ApplicationException(UtilisateurErreur.COMPTE_DEJA_ACTIF, "La personne peut déjà se connecter");
        }

        // 3. Nouveau jeton + email
        String jeton = jetonActivationService.creerJeton(utilisateur);
        emailService.envoyerInvitation(utilisateur.getEmail(), utilisateur.getPrenom(), jeton);
    }

    /**
     * Liste le personnel de l'établissement (médecins, secrétaires, pharmaciens), trié par nom.
     * Chacun indique son statut dans l'établissement (ACTIVE ou SUSPENDUE) et si son compte peut se connecter (actif).
     */
    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister(Integer etablissementId) {
        exigerEtablissement(etablissementId);
        Map<Integer, StatutAffiliation> statuts = affiliationService.statutsDans(etablissementId, ROLES_PERSONNEL);
        return affiliationService.comptesDe(etablissementId, ROLES_PERSONNEL).stream()
                .map(utilisateur -> {
                    UtilisateurDto dto = utilisateurService.convertiUtilisateurDto(utilisateur);
                    dto.setStatutDansEtablissement(statuts.get(utilisateur.getId()));
                    return dto;
                })
                .toList();
    }

    /**
     * Suspend un membre du personnel DANS CET ÉTABLISSEMENT seulement : son jeton pour cet établissement
     * est refusé dès la requête suivante (voir JwtFiltre), mais il continue de travailler dans ses autres
     * établissements, et son compte reste actif. Un administrateur ne peut donc pas couper l'accès d'une personne ailleurs.
     * Ne fait rien si elle est déjà suspendue (204 quand même).
     */
    @Transactional
    public void desactiver(Integer id, Integer etablissementId) {
        Utilisateur utilisateur = trouverPersonnel(id, etablissementId);
        affiliationService.changerStatut(utilisateur.getId(), etablissementId, ROLES_PERSONNEL, StatutAffiliation.SUSPENDUE);
    }

    /**
     * Rend à un membre du personnel suspendu l'accès à cet établissement, avec le même mot de passe.
     * Ne fait rien s'il n'était pas suspendu (204 quand même).
     */
    @Transactional
    public void reactiver(Integer id, Integer etablissementId) {
        Utilisateur utilisateur = trouverPersonnel(id, etablissementId);
        affiliationService.changerStatut(utilisateur.getId(), etablissementId, ROLES_PERSONNEL, StatutAffiliation.ACTIVE);
    }

    /**
     * Retrouve un membre du personnel de cet établissement. Même réponse 404 pour un id inconnu, un patient,
     * un administrateur ou le personnel d'un autre établissement : on ne révèle pas qui existe ailleurs.
     */
    private Utilisateur trouverPersonnel(Integer id, Integer etablissementId) {
        exigerEtablissement(etablissementId);
        return utilisateurRepository.findById(id)
                .filter(utilisateur -> ROLES_PERSONNEL.contains(utilisateur.getRole()))
                .filter(utilisateur -> affiliationService.faitPartieDe(utilisateur.getId(), etablissementId, ROLES_PERSONNEL))
                .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE,
                        "Aucun membre du personnel de votre établissement ne porte cet identifiant"));
    }

    /**
     * Sécurité en plus : la permission PERSONNEL_GERER n'est donnée qu'avec un établissement dans le jeton,
     * donc ce cas ne devrait jamais arriver. S'il arrive, on refuse plutôt que d'agir sur tous les établissements.
     */
    private void exigerEtablissement(Integer etablissementId) {
        if (etablissementId == null) {
            throw new ApplicationException(SecuriteErreur.ETABLISSEMENT_NON_CHOISI,
                    "Choisissez d'abord l'établissement où vous travaillez : POST /auth/etablissement.");
        }
    }

    /** Crée l'objet qui correspond au rôle demandé. Refuse PATIENT et ADMINISTRATEUR. */
    private Utilisateur creerSelonRole(PersonnelCreationDto dto) {
        return switch (dto.getRole()) {
            case MEDECIN -> creerMedecin(dto);
            case SECRETAIRE -> new Secretaire();
            case PHARMACIEN -> new Pharmacien();
            default -> throw new ApplicationException(UtilisateurErreur.ROLE_NON_AUTORISE,
                    "Seuls MEDECIN, SECRETAIRE et PHARMACIEN peuvent être créés ici");
        };
    }

    /** Un médecin a besoin de sa spécialité et de son numéro d'ordre. */
    private Medecin creerMedecin(PersonnelCreationDto dto) {

        if (dto.getSpecialite() == null || dto.getSpecialite().isBlank()
                || dto.getNumeroOrdre() == null || dto.getNumeroOrdre().isBlank()) {
            throw new ApplicationException(UtilisateurErreur.INFORMATIONS_MEDECIN_MANQUANTES,
                    "Renseignez la spécialité et le numéro d'ordre");
        }

        String numeroOrdre = dto.getNumeroOrdre().trim();
        if (medecinRepository.existsByNumeroOrdre(numeroOrdre)) {
            throw new ApplicationException(UtilisateurErreur.NUMERO_ORDRE_DEJA_UTILISE,
                    "Un autre médecin a déjà ce numéro d'ordre");
        }

        Medecin medecin = new Medecin();
        medecin.setSpecialite(dto.getSpecialite().trim());
        medecin.setNumeroOrdre(numeroOrdre);
        return medecin;
    }
}
