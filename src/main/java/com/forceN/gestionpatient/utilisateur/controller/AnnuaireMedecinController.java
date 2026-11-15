package com.forceN.gestionpatient.utilisateur.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.utilisateur.dto.MedecinPublicDto;
import com.forceN.gestionpatient.utilisateur.service.AnnuaireService;

import lombok.RequiredArgsConstructor;

/**
 * L'annuaire public des médecins d'un établissement, consultable SANS connexion (voir SecurityConfig, GET /annuaire/**).
 * La route est sous /annuaire/etablissements, mais elle vit dans le module utilisateur car les médecins sont des comptes.
 */
@RestController
@RequestMapping("/annuaire/etablissements/{etablissementId}/medecins")
@RequiredArgsConstructor
public class AnnuaireMedecinController {

    private final AnnuaireService annuaireService;

    // Filtre facultatif : ?specialite=Cardiologie. 404 si l'établissement n'existe pas ou est désactivé.
    @GetMapping
    public List<MedecinPublicDto> lister(@PathVariable Integer etablissementId,
            @RequestParam(required = false) String specialite) {
        return annuaireService.medecins(etablissementId, specialite);
    }
}
