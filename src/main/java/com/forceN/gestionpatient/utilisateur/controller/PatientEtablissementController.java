package com.forceN.gestionpatient.utilisateur.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.etablissement.dto.EtablissementPublicDto;
import com.forceN.gestionpatient.securite.service.CompteAuthentifie;
import com.forceN.gestionpatient.utilisateur.service.PatientService;

import lombok.RequiredArgsConstructor;

/**
 * Les cliniques du patient connecté (permission ETABLISSEMENT_REJOINDRE, donnée au patient seulement).
 * « moi » : le patient agit toujours pour lui-même, l'id vient du jeton et jamais de l'adresse.
 */
@RestController
@RequestMapping("/patients/moi/etablissements")
@RequiredArgsConstructor
public class PatientEtablissementController {

    private final PatientService patientService;

    // 204, même s'il en faisait déjà partie. 404 établissement inconnu ou désactivé, 403 ADHESION_SUSPENDUE.
    @PostMapping("/{etablissementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ETABLISSEMENT_REJOINDRE')")
    public void rejoindre(@AuthenticationPrincipal CompteAuthentifie connecte, @PathVariable Integer etablissementId) {
        patientService.rejoindre(connecte.email(), etablissementId);
    }

    // Les établissements actifs qu'il a rejoints, avec leur fiche publique.
    @GetMapping
    @PreAuthorize("hasAuthority('ETABLISSEMENT_REJOINDRE')")
    public List<EtablissementPublicDto> lister(@AuthenticationPrincipal CompteAuthentifie connecte) {
        return patientService.mesEtablissements(connecte.email());
    }
}
