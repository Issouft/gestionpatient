package com.forceN.gestionpatient.utilisateur.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.utilisateur.dto.AdministrateurInvitationDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.service.PersonnelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Le super-administrateur invite l'administrateur d'un établissement (permission ETABLISSEMENT_GERER).
 * La route est sous /etablissements, mais elle vit dans le module utilisateur car c'est lui qui crée les comptes.
 */
@RestController
@RequestMapping("/etablissements/{etablissementId}/administrateurs")
@RequiredArgsConstructor
public class AdministrateurEtablissementController {

    private final PersonnelService personnelService;

    // 201 : compte inactif créé, lien d'activation envoyé. 404 établissement inconnu, 409 email déjà pris.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public UtilisateurDto inviter(@PathVariable Integer etablissementId,
            @Valid @RequestBody AdministrateurInvitationDto dto) {
        return personnelService.inviterAdministrateur(dto, etablissementId);
    }
}
