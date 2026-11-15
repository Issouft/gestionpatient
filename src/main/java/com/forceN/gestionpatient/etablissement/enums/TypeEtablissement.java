package com.forceN.gestionpatient.etablissement.enums;

/**
 * Les sortes d'établissements de la plateforme.
 * Pour l'instant seule CLINIQUE est utilisée ; les autres permettront d'ajouter
 * des cabinets, des pharmacies ou des laboratoires sans changer les tables.
 */
public enum TypeEtablissement {
    CLINIQUE, CABINET, PHARMACIE, LABORATOIRE
}
