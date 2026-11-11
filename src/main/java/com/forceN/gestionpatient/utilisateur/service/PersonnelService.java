package com.forceN.gestionpatient.utilisateur.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
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
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/** Gestion du personnel (médecin, secrétaire, pharmacien) par l'administrateur : création, invitation, liste, désactivation. */
@Service
@RequiredArgsConstructor
public class PersonnelService {

    // Les rôles que l'administrateur gère ici. Ni patient (il s'inscrit seul), ni administrateur.
    private static final Set<Role> ROLES_PERSONNEL = EnumSet.of(Role.MEDECIN, Role.SECRETAIRE, Role.PHARMACIEN);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JetonActivationService jetonActivationService;
    private final EmailService emailService;
    private final UtilisateurService utilisateurService;

    /**
     * Crée un compte inactif pour un membre du personnel et lui envoie le lien d'activation.
     * Il choisira lui-même son mot de passe : l'administrateur ne le connaît jamais.
     */
    // Tout ou rien : si l'email ne part pas, le compte et le jeton sont annulés.
    @Transactional
    public UtilisateurDto creer(PersonnelCreationDto dto) {
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

        // 4. Enregistrer, puis créer le jeton et envoyer l'email
        Utilisateur enregistre = utilisateurRepository.save(personnel);
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
    public void renvoyerInvitation(Integer id) {
        // 1. Retrouver le compte, sinon 404
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE,
                        "Aucun compte ne porte cet identifiant"));

        // 2. Refuser si le compte est déjà actif (409)
        if (utilisateur.isActif()) {
            throw new ApplicationException(UtilisateurErreur.COMPTE_DEJA_ACTIF, "La personne peut déjà se connecter");
        }

        // 3. Nouveau jeton + email
        String jeton = jetonActivationService.creerJeton(utilisateur);
        emailService.envoyerInvitation(utilisateur.getEmail(), utilisateur.getPrenom(), jeton);
    }

    /** Liste le personnel (médecins, secrétaires, pharmaciens), actif ou non, trié par nom. */
    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister() {
        return utilisateurRepository.findByRoleInOrderByNomAscPrenomAsc(ROLES_PERSONNEL).stream()
                .map(utilisateurService::convertiUtilisateurDto)
                .toList();
    }

    /**
     * Désactive le compte d'un membre du personnel : il ne peut plus se connecter,
     * et son jeton de connexion actuel est refusé dès la requête suivante (voir JwtFiltre).
     * Pour le réactiver, l'administrateur lui renvoie une invitation : la personne choisit un nouveau mot de passe.
     */
    @Transactional
    public void desactiver(Integer id) {
        Utilisateur utilisateur = trouverPersonnel(id);
        utilisateur.setActif(false);
        // Un lien encore valable (invitation ou mot de passe oublié) ne doit pas pouvoir le réactiver.
        jetonActivationService.annulerJetons(utilisateur);
    }

    /** Retrouve un membre du personnel. 404 aussi pour un patient ou un administrateur : ils ne sont pas gérés ici. */
    private Utilisateur trouverPersonnel(Integer id) {
        return utilisateurRepository.findById(id)
                .filter(utilisateur -> ROLES_PERSONNEL.contains(utilisateur.getRole()))
                .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE,
                        "Aucun membre du personnel ne porte cet identifiant"));
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

        Medecin medecin = new Medecin();
        medecin.setSpecialite(dto.getSpecialite());
        medecin.setNumeroOrdre(dto.getNumeroOrdre());
        return medecin;
    }
}
