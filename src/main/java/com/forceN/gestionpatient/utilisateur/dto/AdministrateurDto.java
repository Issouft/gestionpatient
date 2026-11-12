package com.forceN.gestionpatient.utilisateur.dto;

import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import lombok.*;

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
    private String telephone;

    // Le mot de passe n'est volontairement jamais renvoyé par fromEntity.
    // Il n'est lu que dans toEntity (création / modification).
    private String motDePasse;

    public static AdministrateurDto fromEntity(Administrateur administrateur) {
        if (administrateur == null) {
            return null;
        }
        return AdministrateurDto.builder()
                .id(administrateur.getId())
                .nom(administrateur.getNom())
                .prenom(administrateur.getPrenom())
                .email(administrateur.getEmail())
                .telephone(administrateur.getTelephone())
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
        administrateur.setTelephone(dto.getTelephone());
        administrateur.setMotDePasse(dto.getMotDePasse());

        return administrateur;
    }
}