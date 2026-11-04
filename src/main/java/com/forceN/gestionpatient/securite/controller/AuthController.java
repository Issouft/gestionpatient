package com.forceN.gestionpatient.securite.controller;

import com.forceN.gestionpatient.securite.dto.ConnexionReponse;
import com.forceN.gestionpatient.securite.dto.ConnexionRequete;
import com.forceN.gestionpatient.securite.service.AuthService;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.PatientInscriptionDto;
import com.forceN.gestionpatient.utilisateur.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PatientService patientService;

    // Route publique (voir SecurityConfig) : le visiteur n'a pas encore de compte, donc pas de jeton.
    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto inscrire(@Valid @RequestBody PatientInscriptionDto dto) {
        return patientService.inscrire(dto);
    }

    @PostMapping("/connexion")
    public ConnexionReponse connecter(@RequestBody ConnexionRequete requete) {
        return authService.connecter(requete);
    }
}
