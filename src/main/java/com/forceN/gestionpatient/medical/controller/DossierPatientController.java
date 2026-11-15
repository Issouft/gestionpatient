package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.DossierPatientDto;
import com.forceN.gestionpatient.medical.service.DossierPatientService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Dossiers des patients. 401 sans jeton, 403 avec le jeton d'un autre rôle. */
@RestController
@RequestMapping("/dossiers")
@RequiredArgsConstructor
public class DossierPatientController {

    private final DossierPatientService dossierPatientService;

    // Réservé à la secrétaire. Elle est notée comme créatrice du dossier.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DOSSIER_CREER')")
    public DossierPatientDto creer(@AuthenticationPrincipal UserDetails connecte,
            @Valid @RequestBody DossierPatientDto dto) {
        return dossierPatientService.creer(connecte.getUsername(), dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOSSIER_LIRE')")
    public DossierPatientDto trouver(@PathVariable Integer id) {
        return dossierPatientService.trouver(id);
    }

    // Le dossier d'un patient, à partir de l'id du patient.
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('DOSSIER_LIRE')")
    public DossierPatientDto trouverParPatient(@PathVariable Integer patientId) {
        return dossierPatientService.trouverParPatient(patientId);
    }
}
