package com.forceN.gestionpatient.etablissement.dto;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;

/**
 * Un établissement tel que le voit le public dans l'annuaire : seulement ce qui sert à le choisir et à le contacter.
 * Pas de champ "actif" : l'annuaire ne montre que les établissements actifs.
 */
public record EtablissementPublicDto(
        Integer id,
        String nom,
        TypeEtablissement type,
        String adresse,
        String ville,
        String telephone,
        String email) {

    public static EtablissementPublicDto fromEntity(Etablissement etablissement) {
        return new EtablissementPublicDto(
                etablissement.getId(),
                etablissement.getNom(),
                etablissement.getType(),
                etablissement.getAdresse(),
                etablissement.getVille(),
                etablissement.getTelephone(),
                etablissement.getEmail());
    }
}
