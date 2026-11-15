package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.LigneOrdonnanceDto;
import com.forceN.gestionpatient.medical.dto.OrdonnanceDto;
import com.forceN.gestionpatient.medical.entities.Consultation;
import com.forceN.gestionpatient.medical.entities.LigneOrdonnance;
import com.forceN.gestionpatient.medical.entities.Ordonnance;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.OrdonnanceRepository;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Ordonnances : le médecin prescrit des médicaments à la suite d'une consultation. */
@Service
@RequiredArgsConstructor
public class OrdonnanceService {

    private final OrdonnanceRepository ordonnanceRepository;
    private final ConsultationService consultationService;
    private final MedicamentService medicamentService;
    private final CompteConnecteService compteConnecteService;

    /**
     * Crée l'ordonnance d'une consultation. Seul le médecin qui a fait la consultation peut la rédiger,
     * et une consultation n'a qu'une ordonnance. La prescription ne touche pas au stock :
     * il baisse quand le pharmacien confirme que le patient a pris le produit.
     */
    @Transactional
    public OrdonnanceDto creer(String emailMedecin, Integer consultationId, OrdonnanceDto dto) {
        Medecin medecin = compteConnecteService.medecin(emailMedecin);

        // 1. La consultation existe et c'est celle de ce médecin
        Consultation consultation = consultationService.trouverEntite(consultationId);
        if (!consultation.getMedecin().getId().equals(medecin.getId())) {
            throw new ApplicationException(MedicalErreur.AUTRE_MEDECIN,
                    "Seul le médecin qui a fait la consultation peut rédiger son ordonnance");
        }

        // 2. Une seule ordonnance par consultation
        if (ordonnanceRepository.existsByConsultationId(consultationId)) {
            throw new ApplicationException(MedicalErreur.ORDONNANCE_DEJA_EXISTANTE,
                    "Cette consultation a déjà une ordonnance");
        }

        // 3. L'ordonnance et ses lignes (DTO → entité). Chaque médicament doit exister (404 sinon).
        Ordonnance ordonnance = Ordonnance.builder()
                .dateEmission(Instant.now())
                .consultation(consultation)
                .build();
        for (LigneOrdonnanceDto ligneDto : dto.getLignes()) {
            LigneOrdonnance ligne = ligneDto.toEntity();
            ligne.setMedicament(medicamentService.trouverEntite(ligneDto.getMedicamentId()));
            ordonnance.ajouterLigne(ligne);
        }

        // Les lignes sont enregistrées avec l'ordonnance (cascade dans Ordonnance).
        return OrdonnanceDto.fromEntity(ordonnanceRepository.save(ordonnance));
    }

    @Transactional(readOnly = true)
    public OrdonnanceDto trouver(Integer id) {
        return ordonnanceRepository.findById(id)
                .map(OrdonnanceDto::fromEntity)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.ORDONNANCE_INTROUVABLE,
                        "Aucune ordonnance ne porte cet identifiant"));
    }

    @Transactional(readOnly = true)
    public OrdonnanceDto trouverParConsultation(Integer consultationId) {
        consultationService.trouverEntite(consultationId);
        return ordonnanceRepository.findByConsultationId(consultationId)
                .map(OrdonnanceDto::fromEntity)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.ORDONNANCE_INTROUVABLE,
                        "Cette consultation n'a pas d'ordonnance"));
    }
}
