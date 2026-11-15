package com.forceN.gestionpatient.utilisateur.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Médecin renvoyé au client. Les champs communs (id, nom, email...) viennent de UtilisateurDto. */
@Getter
@Setter
@NoArgsConstructor
public class MedecinDto extends UtilisateurDto {

    private String specialite;

    private String numeroOrdre;
}
