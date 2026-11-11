package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.enums.StatutDemande;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DemandeDisponibiliteDto {

    private LocalDateTime dateDemande;

    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String reponse;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer patientId;

    private Integer medicamentId;

    private Integer pharmacienId;

    /*
    public void valider(String reponse) {
        this.statut = StatutDemande.VALIDEE;
        this.reponse = reponse;
    }

    public void refuser(String reponse) {
        this.statut = StatutDemande.REFUSEE;
        this.reponse = reponse;
    }

     */
}
