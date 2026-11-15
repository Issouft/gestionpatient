package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.DemandeDisponibiliteDto;
import com.forceN.gestionpatient.medical.entities.DemandeDisponibilite;
import com.forceN.gestionpatient.medical.entities.Medicament;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.DemandeDisponibiliteRepository;
import com.forceN.gestionpatient.medical.repository.MedicamentRepository;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Demandes de disponibilité :
 * 1. le patient demande une quantité d'un médicament ;
 * 2. le pharmacien valide (le produit est disponible) ou refuse ;
 * 3. quand le patient vient le chercher, le pharmacien confirme le retrait : le stock baisse à ce moment-là.
 */
@Service
@RequiredArgsConstructor
public class DemandeDisponibiliteService {

    private static final String REPONSE_VALIDATION = "Le produit est disponible. Vous pouvez venir le chercher.";
    private static final String REPONSE_REFUS = "Le produit n'est pas disponible pour le moment.";

    private final DemandeDisponibiliteRepository demandeRepository;
    private final MedicamentRepository medicamentRepository;
    private final MedicamentService medicamentService;
    private final CompteConnecteService compteConnecteService;

    @Transactional
    public DemandeDisponibiliteDto creer(String emailPatient, DemandeDisponibiliteDto dto) {
        Patient patient = compteConnecteService.patient(emailPatient);
        Medicament medicament = medicamentService.trouverEntite(dto.getMedicamentId());

        // DTO → entité (quantité), puis la date, le patient et le médicament. Le statut part de EN_ATTENTE.
        DemandeDisponibilite demande = dto.toEntity();
        demande.setDateDemande(Instant.now());
        demande.setPatient(patient);
        demande.setMedicament(medicament);

        return DemandeDisponibiliteDto.fromEntity(demandeRepository.save(demande));
    }

    /** Les demandes du patient connecté, les plus récentes d'abord : il y lit la réponse du pharmacien. */
    @Transactional(readOnly = true)
    public List<DemandeDisponibiliteDto> mesDemandes(String emailPatient) {
        Patient patient = compteConnecteService.patient(emailPatient);
        return demandeRepository.findByPatientIdOrderByDateDemandeDesc(patient.getId()).stream()
                .map(DemandeDisponibiliteDto::fromEntity)
                .toList();
    }

    /**
     * Pour le pharmacien. Avec un statut : les demandes de ce statut, les plus anciennes d'abord (à traiter en premier).
     * Sans statut : toutes les demandes, les plus récentes d'abord.
     */
    @Transactional(readOnly = true)
    public List<DemandeDisponibiliteDto> lister(StatutDemande statut) {
        List<DemandeDisponibilite> demandes = statut == null
                ? demandeRepository.findAllByOrderByDateDemandeDesc()
                : demandeRepository.findByStatutOrderByDateDemandeAsc(statut);
        return demandes.stream().map(DemandeDisponibiliteDto::fromEntity).toList();
    }

    /** Le produit est disponible : le patient peut venir le chercher. Refusé si le stock ne suffit pas. */
    @Transactional
    public DemandeDisponibiliteDto valider(String emailPharmacien, Integer id, DemandeDisponibiliteDto dto) {
        String reponse = reponseOuDefaut(dto, REPONSE_VALIDATION);
        Pharmacien pharmacien = compteConnecteService.pharmacien(emailPharmacien);
        DemandeDisponibilite demande = trouverEnAttente(id);

        verifierStock(demande.getMedicament(), demande.getQuantite());
        demande.valider(pharmacien, reponse);
        return DemandeDisponibiliteDto.fromEntity(demande);
    }

    @Transactional
    public DemandeDisponibiliteDto refuser(String emailPharmacien, Integer id, DemandeDisponibiliteDto dto) {
        String reponse = reponseOuDefaut(dto, REPONSE_REFUS);
        Pharmacien pharmacien = compteConnecteService.pharmacien(emailPharmacien);
        DemandeDisponibilite demande = trouverEnAttente(id);

        demande.refuser(pharmacien, reponse);
        return DemandeDisponibiliteDto.fromEntity(demande);
    }

    /**
     * Le pharmacien confirme que le patient a pris le produit : le stock baisse de la quantité demandée.
     * Possible une seule fois, et seulement pour une demande validée.
     */
    @Transactional
    public DemandeDisponibiliteDto confirmerRetrait(Integer id) {
        // Verrous : deux confirmations en même temps ne retirent pas deux fois du stock.
        DemandeDisponibilite demande = trouverPourTraitement(id);
        if (demande.getStatut() != StatutDemande.VALIDEE) {
            throw new ApplicationException(MedicalErreur.DEMANDE_NON_VALIDEE,
                    "La demande est " + demande.getStatut() + " : seule une demande VALIDEE peut être retirée");
        }
        if (demande.estRetiree()) {
            throw new ApplicationException(MedicalErreur.DEMANDE_DEJA_RETIREE,
                    "Le retrait de ce produit a déjà été confirmé");
        }

        Medicament medicament = medicamentRepository.trouverPourModifierStock(demande.getMedicament().getId())
                .orElseThrow(() -> new ApplicationException(MedicalErreur.MEDICAMENT_INTROUVABLE,
                        "Le médicament de cette demande n'existe plus"));
        // Le stock a pu baisser depuis la validation (autres retraits) : on vérifie à nouveau.
        verifierStock(medicament, demande.getQuantite());

        // Pas de save() : Hibernate enregistre les changements à la fin de la transaction.
        medicament.retirerDuStock(demande.getQuantite());
        demande.setDateRetrait(Instant.now());
        return DemandeDisponibiliteDto.fromEntity(demande);
    }

    /** Demande à valider ou refuser : elle doit être encore EN_ATTENTE. */
    private DemandeDisponibilite trouverEnAttente(Integer id) {
        DemandeDisponibilite demande = trouverPourTraitement(id);
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new ApplicationException(MedicalErreur.DEMANDE_DEJA_TRAITEE,
                    "La demande est déjà " + demande.getStatut());
        }
        return demande;
    }

    private DemandeDisponibilite trouverPourTraitement(Integer id) {
        return demandeRepository.trouverPourTraitement(id)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.DEMANDE_INTROUVABLE,
                        "Aucune demande ne porte cet identifiant"));
    }

    private void verifierStock(Medicament medicament, int quantite) {
        if (medicament.getStock() < quantite) {
            throw new ApplicationException(MedicalErreur.STOCK_INSUFFISANT,
                    "Stock de " + medicament.getNom() + " : " + medicament.getStock()
                            + ", quantité demandée : " + quantite);
        }
    }

    /**
     * Le message du pharmacien s'il en a écrit un, sinon un message par défaut. Le corps de la requête est facultatif.
     * Longueur vérifiée ici : ces routes n'utilisent pas @Valid, qui exigerait aussi medicamentId.
     */
    private String reponseOuDefaut(DemandeDisponibiliteDto dto, String defaut) {
        if (dto == null || dto.getReponse() == null || dto.getReponse().isBlank()) {
            return defaut;
        }
        String reponse = dto.getReponse().trim();
        if (reponse.length() > DemandeDisponibiliteDto.LONGUEUR_MAX_REPONSE) {
            throw new ApplicationException(MedicalErreur.REPONSE_TROP_LONGUE,
                    "La réponse ne doit pas dépasser " + DemandeDisponibiliteDto.LONGUEUR_MAX_REPONSE + " caractères");
        }
        return reponse;
    }
}
