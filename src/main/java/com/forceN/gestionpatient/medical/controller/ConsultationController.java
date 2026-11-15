package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.ConsultationDto;
import com.forceN.gestionpatient.medical.dto.OrdonnanceDto;
import com.forceN.gestionpatient.medical.service.ConsultationService;
import com.forceN.gestionpatient.medical.service.OrdonnanceService;
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

/** Consultations, et l'ordonnance qui en découle. Le médecin vient du jeton. */
@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final OrdonnanceService ordonnanceService;

    // Réservé au médecin du rendez-vous. Le rendez-vous passe à TERMINE.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MEDECIN')")
    public ConsultationDto creer(@AuthenticationPrincipal UserDetails connecte,
            @Valid @RequestBody ConsultationDto dto) {
        return consultationService.creer(connecte.getUsername(), dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEDECIN')")
    public ConsultationDto trouver(@PathVariable Integer id) {
        return consultationService.trouver(id);
    }

    // Réservé au médecin qui a fait la consultation. Une seule ordonnance par consultation.
    @PostMapping("/{id}/ordonnance")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MEDECIN')")
    public OrdonnanceDto creerOrdonnance(@AuthenticationPrincipal UserDetails connecte,
            @PathVariable Integer id, @Valid @RequestBody OrdonnanceDto dto) {
        return ordonnanceService.creer(connecte.getUsername(), id, dto);
    }

    @GetMapping("/{id}/ordonnance")
    @PreAuthorize("hasAnyRole('MEDECIN', 'PHARMACIEN')")
    public OrdonnanceDto trouverOrdonnance(@PathVariable Integer id) {
        return ordonnanceService.trouverParConsultation(id);
    }
}
