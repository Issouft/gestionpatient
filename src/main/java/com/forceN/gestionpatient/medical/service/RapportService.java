package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.medical.dto.RapportDto;
import com.forceN.gestionpatient.medical.entities.Rapport;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import com.forceN.gestionpatient.medical.exception.MedicalErreur;
import com.forceN.gestionpatient.medical.repository.ConsultationRepository;
import com.forceN.gestionpatient.medical.repository.DemandeDisponibiliteRepository;
import com.forceN.gestionpatient.medical.repository.DossierPatientRepository;
import com.forceN.gestionpatient.medical.repository.MedicamentRepository;
import com.forceN.gestionpatient.medical.repository.OrdonnanceRepository;
import com.forceN.gestionpatient.medical.repository.RapportRepository;
import com.forceN.gestionpatient.rendezvous.enums.StatutRendezVous;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Rapports : l'administrateur choisit un type et une période, l'application calcule les chiffres
 * et les enregistre dans un résumé. La période inclut le premier et le dernier jour.
 */
@Service
@RequiredArgsConstructor
public class RapportService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Les dates sont stockées en Instant : on passe par le fuseau du serveur pour les jours.
    private static final ZoneId FUSEAU = ZoneId.systemDefault();

    private final RapportRepository rapportRepository;
    private final ConsultationRepository consultationRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final DossierPatientRepository dossierPatientRepository;
    private final MedicamentRepository medicamentRepository;
    private final DemandeDisponibiliteRepository demandeRepository;
    private final CompteConnecteService compteConnecteService;

    @Transactional
    public RapportDto generer(String emailAdministrateur, RapportDto dto) {
        if (dto.getPeriodeFin().isBefore(dto.getPeriodeDebut())) {
            throw new ApplicationException(MedicalErreur.PERIODE_INVALIDE,
                    "La date de fin doit être égale ou postérieure à la date de début");
        }

        // Période [debut, finExclue[ : le dernier jour est compté en entier.
        Instant debut = dto.getPeriodeDebut().atStartOfDay(FUSEAU).toInstant();
        Instant finExclue = dto.getPeriodeFin().plusDays(1).atStartOfDay(FUSEAU).toInstant();
        String periode = "Du " + dto.getPeriodeDebut().format(FORMAT_DATE)
                + " au " + dto.getPeriodeFin().format(FORMAT_DATE) + " : ";

        String resume = periode + switch (dto.getType()) {
            case ACTIVITE_MEDICALE -> resumerActiviteMedicale(debut, finExclue);
            case RENDEZ_VOUS -> resumerRendezVous(dto.getPeriodeDebut(), dto.getPeriodeFin());
            case STOCK_MEDICAMENTS -> resumerStock(debut, finExclue);
        };

        Rapport rapport = Rapport.builder()
                .type(dto.getType())
                .periodeDebut(debut)
                .periodeFin(dto.getPeriodeFin().atStartOfDay(FUSEAU).toInstant())
                .dateGeneration(Instant.now())
                .resume(resume)
                .administrateur(compteConnecteService.administrateur(emailAdministrateur))
                .build();
        return RapportDto.fromEntity(rapportRepository.save(rapport), FUSEAU);
    }

    /** Tous les rapports, les plus récents d'abord. */
    @Transactional(readOnly = true)
    public List<RapportDto> lister() {
        return rapportRepository.findAllByOrderByDateGenerationDesc().stream()
                .map(rapport -> RapportDto.fromEntity(rapport, FUSEAU))
                .toList();
    }

    private String resumerActiviteMedicale(Instant debut, Instant fin) {
        return consultationRepository.compterEntre(debut, fin) + " consultation(s) pour "
                + consultationRepository.compterPatientsEntre(debut, fin) + " patient(s), "
                + ordonnanceRepository.compterEntre(debut, fin) + " ordonnance(s), "
                + dossierPatientRepository.compterCreesEntre(debut, fin) + " dossier(s) créé(s).";
    }

    // Les rendez-vous (Partie 2) utilisent LocalDateTime, pas Instant.
    private String resumerRendezVous(LocalDate premierJour, LocalDate dernierJour) {
        LocalDateTime debut = premierJour.atStartOfDay();
        LocalDateTime fin = dernierJour.plusDays(1).atStartOfDay();

        long total = 0;
        StringBuilder detail = new StringBuilder();
        for (StatutRendezVous statut : StatutRendezVous.values()) {
            long nombre = rapportRepository.compterRendezVous(statut, debut, fin);
            total += nombre;
            detail.append(detail.isEmpty() ? "" : ", ").append(statut).append(" : ").append(nombre);
        }
        return total + " rendez-vous (" + detail + ").";
    }

    // Le stock est celui du jour de génération ; les demandes et retraits sont ceux de la période.
    private String resumerStock(Instant debut, Instant fin) {
        long recues = 0;
        StringBuilder detail = new StringBuilder();
        for (StatutDemande statut : StatutDemande.values()) {
            long nombre = demandeRepository.compterParStatutEntre(statut, debut, fin);
            recues += nombre;
            detail.append(detail.isEmpty() ? "" : ", ").append(statut).append(" : ").append(nombre);
        }
        return medicamentRepository.count() + " médicament(s) aujourd'hui, dont "
                + medicamentRepository.compterSousSeuilAlerte() + " sous le seuil d'alerte et "
                + medicamentRepository.countByStock(0) + " en rupture. "
                + recues + " demande(s) reçue(s) (" + detail + "), "
                + demandeRepository.compterRetraitsEntre(debut, fin) + " retrait(s) pour "
                + demandeRepository.compterUnitesRetireesEntre(debut, fin) + " unité(s).";
    }
}
