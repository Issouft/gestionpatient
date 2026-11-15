package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.DemandeDisponibiliteDto;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import com.forceN.gestionpatient.medical.service.DemandeDisponibiliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Demandes de disponibilité d'un médicament.
 * Le patient demande ; le pharmacien valide ou refuse, puis confirme le retrait (le stock baisse alors).
 */
@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeDisponibiliteController {

    private final DemandeDisponibiliteService demandeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATIENT')")
    public DemandeDisponibiliteDto creer(@AuthenticationPrincipal UserDetails connecte,
            @Valid @RequestBody DemandeDisponibiliteDto dto) {
        return demandeService.creer(connecte.getUsername(), dto);
    }

    // Le patient ne voit que ses propres demandes.
    @GetMapping("/moi")
    @PreAuthorize("hasRole('PATIENT')")
    public List<DemandeDisponibiliteDto> mesDemandes(@AuthenticationPrincipal UserDetails connecte) {
        return demandeService.mesDemandes(connecte.getUsername());
    }

    // Exemple : GET /demandes?statut=EN_ATTENTE pour les demandes à traiter. Sans statut : toutes.
    @GetMapping
    @PreAuthorize("hasRole('PHARMACIEN')")
    public List<DemandeDisponibiliteDto> lister(@RequestParam(required = false) StatutDemande statut) {
        return demandeService.lister(statut);
    }

    // Corps facultatif : {"reponse": "..."}. Sans message, une réponse par défaut est enregistrée.
    // Pas de @Valid : il exigerait medicamentId, utile seulement à la création. Le service vérifie la réponse.
    @PostMapping("/{id}/validation")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public DemandeDisponibiliteDto valider(@AuthenticationPrincipal UserDetails connecte, @PathVariable Integer id,
            @RequestBody(required = false) DemandeDisponibiliteDto dto) {
        return demandeService.valider(connecte.getUsername(), id, dto);
    }

    @PostMapping("/{id}/refus")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public DemandeDisponibiliteDto refuser(@AuthenticationPrincipal UserDetails connecte, @PathVariable Integer id,
            @RequestBody(required = false) DemandeDisponibiliteDto dto) {
        return demandeService.refuser(connecte.getUsername(), id, dto);
    }

    // Le patient a pris le produit : le stock baisse de la quantité demandée. Sans corps.
    @PostMapping("/{id}/retrait")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public DemandeDisponibiliteDto confirmerRetrait(@PathVariable Integer id) {
        return demandeService.confirmerRetrait(id);
    }
}
