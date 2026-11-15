package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.MedicamentDto;
import com.forceN.gestionpatient.medical.entities.Medicament;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Médicaments : le pharmacien les ajoute avec leur stock de départ.
 * Ensuite, le stock baisse tout seul à chaque retrait confirmé (voir DemandeDisponibiliteService).
 */
@Service
@RequiredArgsConstructor
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;
    private final CompteConnecteService compteConnecteService;

    @Transactional
    public MedicamentDto creer(String emailPharmacien, MedicamentDto dto) {
        // DTO → entité (nom et dosage sans espaces autour), puis le pharmacien qui l'ajoute
        Medicament medicament = dto.toEntity();
        if (medicamentRepository.existsByNomIgnoreCaseAndDosageIgnoreCase(medicament.getNom(), medicament.getDosage())) {
            throw new ApplicationException(MedicalErreur.MEDICAMENT_DEJA_EXISTANT,
                    "Le médicament " + medicament.getNom() + " " + medicament.getDosage() + " est déjà enregistré");
        }
        medicament.setPharmacien(compteConnecteService.pharmacien(emailPharmacien));

        return MedicamentDto.fromEntity(medicamentRepository.save(medicament));
    }

    /** Tous les médicaments, triés par nom : le patient y choisit ce qu'il demande, le médecin ce qu'il prescrit. */
    @Transactional(readOnly = true)
    public List<MedicamentDto> lister() {
        return medicamentRepository.findAllByOrderByNomAscDosageAsc().stream()
                .map(MedicamentDto::fromEntity)
                .toList();
    }

    /** Utilisée aussi par les ordonnances et les demandes. 404 si le médicament n'existe pas. */
    Medicament trouverEntite(Integer id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.MEDICAMENT_INTROUVABLE,
                        "Aucun médicament ne porte l'identifiant " + id));
    }
}
