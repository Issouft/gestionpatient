package com.forceN.gestionpatient.medical.controller;

import com.forceN.gestionpatient.medical.dto.RapportDto;
import com.forceN.gestionpatient.medical.service.RapportService;
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

/** Rapports d'activité, réservés à l'administrateur. */
@RestController
@RequestMapping("/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RAPPORT_GERER')")
    public RapportDto generer(@AuthenticationPrincipal UserDetails connecte,
            @Valid @RequestBody RapportDto dto) {
        return rapportService.generer(connecte.getUsername(), dto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RAPPORT_GERER')")
    public List<RapportDto> lister() {
        return rapportService.lister();
    }
}
