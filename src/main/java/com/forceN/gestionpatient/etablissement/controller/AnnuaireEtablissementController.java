package com.forceN.gestionpatient.etablissement.controller;

import com.forceN.gestionpatient.etablissement.dto.EtablissementPublicDto;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.service.EtablissementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * L'annuaire public des établissements : consultable SANS connexion (voir SecurityConfig, GET /annuaire/**),
 * pour qu'un patient choisisse sa clinique avant même de créer son compte.
 * Ne montre que les établissements actifs, et seulement leur fiche publique.
 */
@RestController
@RequestMapping("/annuaire/etablissements")
@RequiredArgsConstructor
public class AnnuaireEtablissementController {

    private final EtablissementService etablissementService;

    // Filtres facultatifs : ?type=CLINIQUE&ville=Thiès. Un type inconnu donne 400.
    @GetMapping
    public List<EtablissementPublicDto> lister(@RequestParam(required = false) TypeEtablissement type,
            @RequestParam(required = false) String ville) {
        return etablissementService.annuaire(type, ville);
    }

    // La fiche d'un établissement. 404 s'il n'existe pas ou est désactivé (même réponse dans les deux cas).
    @GetMapping("/{id}")
    public EtablissementPublicDto fiche(@PathVariable Integer id) {
        return etablissementService.fichePublique(id);
    }
}
