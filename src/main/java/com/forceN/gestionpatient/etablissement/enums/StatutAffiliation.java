package com.forceN.gestionpatient.etablissement.enums;

/** État d'une affiliation : la personne peut-elle agir dans cet établissement ? */
public enum StatutAffiliation {
    // La personne peut travailler dans l'établissement (ou y être suivie, pour un patient).
    ACTIVE,
    // L'établissement a retiré l'accès à cette personne. Ses autres affiliations ne changent pas.
    SUSPENDUE
}
