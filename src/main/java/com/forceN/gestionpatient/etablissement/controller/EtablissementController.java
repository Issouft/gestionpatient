package com.forceN.gestionpatient.etablissement.controller;

import com.forceN.gestionpatient.etablissement.dto.EtablissementDto;
import com.forceN.gestionpatient.etablissement.service.EtablissementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestion des établissements de la plateforme, réservée au super-administrateur (permission ETABLISSEMENT_GERER).
 * 401 sans jeton, 403 avec le jeton d'un autre rôle.
 */
@RestController
@RequestMapping("/etablissements")
@RequiredArgsConstructor
public class EtablissementController {

    private final EtablissementService etablissementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public EtablissementDto creer(@Valid @RequestBody EtablissementDto dto) {
        return etablissementService.creer(dto);
    }

    // Modifie la fiche (mêmes champs et mêmes règles qu'à la création). Ne change pas « actif ».
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public EtablissementDto modifier(@PathVariable Integer id, @Valid @RequestBody EtablissementDto dto) {
        return etablissementService.modifier(id, dto);
    }

    // Tous les établissements, actifs ou non : le champ "actif" le précise.
    @GetMapping
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public List<EtablissementDto> lister() {
        return etablissementService.lister();
    }

    // Tout le personnel de l'établissement perd l'accès tout de suite. 204, même s'il était déjà inactif.
    @PostMapping("/{id}/desactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public void desactiver(@PathVariable Integer id) {
        etablissementService.desactiver(id);
    }

    // Le personnel retrouve l'accès. 204, même s'il était déjà actif.
    @PostMapping("/{id}/reactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ETABLISSEMENT_GERER')")
    public void reactiver(@PathVariable Integer id) {
        etablissementService.reactiver(id);
    }
}
