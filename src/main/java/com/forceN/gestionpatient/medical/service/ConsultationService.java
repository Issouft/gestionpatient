package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.ConsultationDto;
import com.forceN.gestionpatient.medical.entities.Consultation;
import com.forceN.gestionpatient.medical.entities.DossierPatient;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.ConsultationRepository;
import com.forceN.gestionpatient.medical.repository.DossierPatientRepository;
import com.forceN.gestionpatient.rendezvous.entities.RendezVous;
import com.forceN.gestionpatient.rendezvous.enums.StatutRendezVous;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Consultations : le médecin consulte un patient à partir d'un rendez-vous confirmé. */
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final DossierPatientRepository dossierPatientRepository;
    private final CompteConnecteService compteConnecteService;
    // La Partie 2 n'a pas encore de RendezVousRepository : on lit le rendez-vous directement.
    // À remplacer par RendezVousRepository quand il existera.
    private final EntityManager entityManager;

    /**
     * Enregistre la consultation d'un rendez-vous et l'ajoute au dossier du patient.
     * Le rendez-vous passe alors à TERMINE.
     */
    @Transactional
    public ConsultationDto creer(String emailMedecin, ConsultationDto dto) {
        Medecin medecin = compteConnecteService.medecin(emailMedecin);

        // 1. Le rendez-vous existe et c'est celui de ce médecin
        RendezVous rendezVous = entityManager.find(RendezVous.class, dto.getRendezVousId());
        if (rendezVous == null) {
            throw new ApplicationException(MedicalErreur.RENDEZ_VOUS_INTROUVABLE,
                    "Aucun rendez-vous ne porte cet identifiant");
        }
        if (!rendezVous.getMedecin().getId().equals(medecin.getId())) {
            throw new ApplicationException(MedicalErreur.AUTRE_MEDECIN,
                    "Ce rendez-vous est celui d'un autre médecin");
        }

        // 2. Une seule consultation par rendez-vous. Vérifié avant le statut :
        //    un rendez-vous déjà consulté est TERMINE, et ce message-ci est plus clair.
        if (consultationRepository.existsByRendezVousId(rendezVous.getId())) {
            throw new ApplicationException(MedicalErreur.CONSULTATION_DEJA_EXISTANTE,
                    "Ce rendez-vous a déjà donné lieu à une consultation");
        }

        // 3. Seul un rendez-vous confirmé donne lieu à une consultation
        if (rendezVous.getStatut() != StatutRendezVous.CONFIRME) {
            throw new ApplicationException(MedicalErreur.RENDEZ_VOUS_NON_CONFIRME,
                    "Le rendez-vous est " + rendezVous.getStatut() + " : seul un rendez-vous CONFIRME peut être consulté");
        }

        // 4. Le patient doit avoir un dossier, créé par la secrétaire
        DossierPatient dossier = dossierPatientRepository.findByPatientId(rendezVous.getPatient().getId())
                .orElseThrow(() -> new ApplicationException(MedicalErreur.DOSSIER_INTROUVABLE,
                        "Ce patient n'a pas encore de dossier : la secrétaire doit d'abord le créer"));

        // 5. DTO → entité, ajout au dossier, et fin du rendez-vous
        Consultation consultation = dto.toEntity();
        consultation.setDate(Instant.now());
        consultation.setMedecin(medecin);
        consultation.setRendezVous(rendezVous);
        dossier.ajouterConsultation(consultation);
        consultationRepository.save(consultation);
        // Pas de save() : Hibernate enregistre le nouveau statut à la fin de la transaction.
        rendezVous.setStatut(StatutRendezVous.TERMINE);

        return ConsultationDto.fromEntity(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationDto trouver(Integer id) {
        return ConsultationDto.fromEntity(trouverEntite(id));
    }

    /** Utilisée aussi par OrdonnanceService. 404 si la consultation n'existe pas. */
    Consultation trouverEntite(Integer id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(MedicalErreur.CONSULTATION_INTROUVABLE,
                        "Aucune consultation ne porte cet identifiant"));
    }
}
