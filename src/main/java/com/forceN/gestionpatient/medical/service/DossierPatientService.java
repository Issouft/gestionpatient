package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.DossierPatientDto;
import com.forceN.gestionpatient.medical.entities.DossierPatient;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.DossierPatientRepository;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dossiers des patients : créés par la secrétaire, lus par la secrétaire et le médecin. */
@Service
@RequiredArgsConstructor
public class DossierPatientService {

    private final DossierPatientRepository dossierPatientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CompteConnecteService compteConnecteService;

    /** Crée le dossier d'un patient. Un patient n'a qu'un seul dossier. */
    @Transactional
    public DossierPatientDto creer(String emailSecretaire, DossierPatientDto dto) {
        // 1. Le patient doit exister (un autre rôle avec cet id ne compte pas)
        Patient patient = utilisateurRepository.findById(dto.getPatientId())
                .filter(Patient.class::isInstance)
                .map(Patient.class::cast)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.PATIENT_INTROUVABLE,
                        "Aucun patient ne porte cet identifiant"));

        // 2. Refus s'il a déjà un dossier (409)
        if (dossierPatientRepository.existsByPatientId(patient.getId())) {
            throw new ApplicationException(MedicalErreur.DOSSIER_DEJA_EXISTANT,
                    "Ce patient a déjà un dossier. Consultez-le au lieu d'en créer un autre.");
        }

        // 3. DTO → entité, puis le patient et la secrétaire qui crée le dossier
        DossierPatient dossier = dto.toEntity();
        dossier.setPatient(patient);
        dossier.setCreePar(compteConnecteService.secretaire(emailSecretaire));

        return DossierPatientDto.fromEntity(dossierPatientRepository.save(dossier));
    }

    @Transactional(readOnly = true)
    public DossierPatientDto trouver(Integer id) {
        return dossierPatientRepository.findById(id)
                .map(DossierPatientDto::fromEntity)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.DOSSIER_INTROUVABLE,
                        "Aucun dossier ne porte cet identifiant"));
    }

    @Transactional(readOnly = true)
    public DossierPatientDto trouverParPatient(Integer patientId) {
        return dossierPatientRepository.findByPatientId(patientId)
                .map(DossierPatientDto::fromEntity)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.DOSSIER_INTROUVABLE,
                        "Ce patient n'a pas encore de dossier"));
    }
}
