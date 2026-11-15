package com.forceN.gestionpatient.utilisateur.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.securite.service.CompteAuthentifie;
import com.forceN.gestionpatient.utilisateur.dto.PersonnelCreationDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.service.PersonnelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Gestion du personnel par l'administrateur. Chaque route agit seulement dans l'établissement
 * du jeton de l'administrateur (connecte.etablissementId()).
 */
@RestController
@RequestMapping("/personnel")
@RequiredArgsConstructor
public class PersonnelController {
    private final PersonnelService personnelService;

    // Réservé à l'admin : 401 sans jeton, 403 avec un jeton d'un autre rôle.
    // Le compte est créé inactif ; un email d'activation est envoyé à la personne.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERSONNEL_GERER')")
    public UtilisateurDto creer(@AuthenticationPrincipal CompteAuthentifie connecte,
            @Valid @RequestBody PersonnelCreationDto dto) {
        return personnelService.creer(dto, connecte.etablissementId());
    }

    // Réservé à l'admin. Pour une personne qui n'a pas activé son compte à temps.
    // 204 : l'email est parti, il n'y a rien à renvoyer.
    @PostMapping("/{id}/invitation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERSONNEL_GERER')")
    public void renvoyerInvitation(@AuthenticationPrincipal CompteAuthentifie connecte, @PathVariable Integer id) {
        personnelService.renvoyerInvitation(id, connecte.etablissementId());
    }

    // Réservé à l'admin. Le personnel de son établissement, actif ou non : le champ "actif" de chaque compte le précise.
    @GetMapping
    @PreAuthorize("hasAuthority('PERSONNEL_GERER')")
    public List<UtilisateurDto> lister(@AuthenticationPrincipal CompteAuthentifie connecte) {
        return personnelService.lister(connecte.etablissementId());
    }

    // Réservé à l'admin. Suspend la personne dans SON établissement seulement : elle continue de travailler ailleurs.
    // 204, même si elle était déjà suspendue. Pour lui rendre l'accès : la route de réactivation ci-dessous.
    @PostMapping("/{id}/desactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERSONNEL_GERER')")
    public void desactiver(@AuthenticationPrincipal CompteAuthentifie connecte, @PathVariable Integer id) {
        personnelService.desactiver(id, connecte.etablissementId());
    }

    // Réservé à l'admin. Rend l'accès à l'établissement, avec le même mot de passe. 204, même s'il n'était pas suspendu.
    @PostMapping("/{id}/reactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERSONNEL_GERER')")
    public void reactiver(@AuthenticationPrincipal CompteAuthentifie connecte, @PathVariable Integer id) {
        personnelService.reactiver(id, connecte.etablissementId());
    }
}
