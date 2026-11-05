package com.forceN.gestionpatient.utilisateur.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {
    private final UtilisateurService utilisateurService;

    // Route protégée : JwtFiltre a déjà identifié l'utilisateur grâce au jeton.
    // Son username est son email (voir UtilisateurConnecteService).
    @GetMapping("/moi")
    public UtilisateurDto consulterProfil(@AuthenticationPrincipal UserDetails connecte) {
        return utilisateurService.consulterProfil(connecte.getUsername());
    }
}
