package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.MedicamentDto;
import com.forceN.gestionpatient.medical.service.MedicamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Médicaments. Il n'y a pas de route pour changer le stock : il baisse seul à chaque retrait confirmé. */
@RestController
@RequestMapping("/medicaments")
@RequiredArgsConstructor
public class MedicamentController {

    private final MedicamentService medicamentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MEDICAMENT_GERER')")
    public MedicamentDto creer(@AuthenticationPrincipal UserDetails connecte,
            @Valid @RequestBody MedicamentDto dto) {
        return medicamentService.creer(connecte.getUsername(), dto);
    }

    // Ouvert à toute personne connectée : le patient choisit ce qu'il demande, le médecin ce qu'il prescrit.
    @GetMapping
    public List<MedicamentDto> lister() {
        return medicamentService.lister();
    }
}
