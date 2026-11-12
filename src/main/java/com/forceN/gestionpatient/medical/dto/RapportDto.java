package com.forceN.gestionpatient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forceN.gestionpatient.medical.entities.Rapport;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdministrateurDto {

    private Integer id;

    private String nom;
    private String prenom;
    private String email;
    private String contact;

    // Écrit uniquement (création / modification), jamais renvoyé au front
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String motDePasse;

    // Uniquement les ids des rapports, pour éviter la boucle infinie avec RapportDto
    @Builder.Default
    private List<Integer> rapportIds = new ArrayList<>();

    public static AdministrateurDto fromEntity(Administrateur administrateur) {
        if (administrateur == null) {
            return null;
        }
        return AdministrateurDto.builder()
                .id(administrateur.getId())
                .nom(administrateur.getNom())
                .prenom(administrateur.getPrenom())
                .email(administrateur.getEmail())
                .contact(administrateur.getContact())
                .rapportIds(
                        administrateur.getRapports() == null
                                ? new ArrayList<>()
                                : administrateur.getRapports().stream()
                                .map(Rapport::getId)
                                .toList()
                )
                .build();
    }

    public static Administrateur toEntity(AdministrateurDto dto) {
        if (dto == null) {
            return null;
        }

        Administrateur administrateur = new Administrateur();
        administrateur.setId(dto.getId());
        administrateur.setNom(dto.getNom());
        administrateur.setPrenom(dto.getPrenom());
        administrateur.setEmail(dto.getEmail());
        administrateur.setContact(dto.getContact());
        // administrateur.setMotDePasse(dto.getMotDePasse());
        // Les rapports ne sont pas reconstruits ici : la relation est portée
        // par Rapport (qui référence l'administrateur).

        return administrateur;
    }
}