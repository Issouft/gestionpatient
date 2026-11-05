package com.forceN.gestionpatient.utilisateur.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.utilisateur.dto.PersonnelCreationDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.service.PersonnelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Gestion du personnel par l'administrateur. */
@RestController
@RequestMapping("/personnel")
@RequiredArgsConstructor
public class PersonnelController {
    private final PersonnelService personnelService;

    // Réservé à l'admin : 401 sans jeton, 403 avec un jeton d'un autre rôle.
    // Le compte est créé inactif ; un email d'activation est envoyé à la personne.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public UtilisateurDto creer(@Valid @RequestBody PersonnelCreationDto dto) {
        return personnelService.creer(dto);
    }
}
