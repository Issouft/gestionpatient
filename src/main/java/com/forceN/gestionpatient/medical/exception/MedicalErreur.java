package com.forceN.gestionpatient.medical.exception;

import com.forceN.gestionpatient.commun.exception.CodeErreur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MedicalErreur implements CodeErreur {
    // Ajouter les erreurs ici, avant le point-virgule. Exemple : NOM_ERREUR(HttpStatus.NOT_FOUND, "Titre de l'erreur");

    // Dossier patient
    PATIENT_INTROUVABLE(HttpStatus.NOT_FOUND, "Patient introuvable"),
    // Un patient n'a qu'un seul dossier.
    DOSSIER_DEJA_EXISTANT(HttpStatus.CONFLICT, "Le patient a déjà un dossier"),
    DOSSIER_INTROUVABLE(HttpStatus.NOT_FOUND, "Dossier introuvable"),

    // Consultation et ordonnance
    RENDEZ_VOUS_INTROUVABLE(HttpStatus.NOT_FOUND, "Rendez-vous introuvable"),
    // Seul un rendez-vous confirmé peut donner lieu à une consultation.
    RENDEZ_VOUS_NON_CONFIRME(HttpStatus.CONFLICT, "Le rendez-vous n'est pas confirmé"),
    // Un médecin ne travaille que sur ses propres rendez-vous et ses propres consultations.
    AUTRE_MEDECIN(HttpStatus.FORBIDDEN, "Cette ressource appartient à un autre médecin"),
    CONSULTATION_DEJA_EXISTANTE(HttpStatus.CONFLICT, "Ce rendez-vous a déjà une consultation"),
    CONSULTATION_INTROUVABLE(HttpStatus.NOT_FOUND, "Consultation introuvable"),
    ORDONNANCE_DEJA_EXISTANTE(HttpStatus.CONFLICT, "Cette consultation a déjà une ordonnance"),
    ORDONNANCE_INTROUVABLE(HttpStatus.NOT_FOUND, "Ordonnance introuvable"),

    // Médicaments et demandes de disponibilité
    MEDICAMENT_INTROUVABLE(HttpStatus.NOT_FOUND, "Médicament introuvable"),
    MEDICAMENT_DEJA_EXISTANT(HttpStatus.CONFLICT, "Ce médicament existe déjà"),
    DEMANDE_INTROUVABLE(HttpStatus.NOT_FOUND, "Demande introuvable"),
    // On ne valide ni ne refuse une demande déjà traitée.
    DEMANDE_DEJA_TRAITEE(HttpStatus.CONFLICT, "La demande a déjà été traitée"),
    // Le retrait n'est possible que pour une demande validée, et une seule fois.
    DEMANDE_NON_VALIDEE(HttpStatus.CONFLICT, "La demande n'est pas validée"),
    DEMANDE_DEJA_RETIREE(HttpStatus.CONFLICT, "Le produit a déjà été retiré"),
    STOCK_INSUFFISANT(HttpStatus.CONFLICT, "Stock insuffisant"),
    REPONSE_TROP_LONGUE(HttpStatus.BAD_REQUEST, "La réponse est trop longue"),

    // Rapports
    PERIODE_INVALIDE(HttpStatus.BAD_REQUEST, "La période est invalide")
    ;

    private final HttpStatus statut;
    private final String titre;
}
