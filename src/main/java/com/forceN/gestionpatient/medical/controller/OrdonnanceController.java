package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.OrdonnanceDto;
import com.forceN.gestionpatient.medical.service.OrdonnanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lecture d'une ordonnance par son id. Elle se crée depuis sa consultation : POST /consultations/{id}/ordonnance. */
@RestController
@RequestMapping("/ordonnances")
@RequiredArgsConstructor
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDONNANCE_LIRE')")
    public OrdonnanceDto trouver(@PathVariable Integer id) {
        return ordonnanceService.trouver(id);
    }
}
