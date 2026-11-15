package com.forceN.gestionpatient.securite.controller;

import com.forceN.gestionpatient.securite.dto.ChoixEtablissementRequete;
import com.forceN.gestionpatient.securite.dto.ChoixMotDePasseRequete;
import com.forceN.gestionpatient.securite.dto.ConnexionReponse;
import com.forceN.gestionpatient.securite.dto.ConnexionRequete;
import com.forceN.gestionpatient.securite.dto.MotDePasseOublieRequete;
import com.forceN.gestionpatient.securite.service.AuthService;
import com.forceN.gestionpatient.securite.service.CompteAuthentifie;
import com.forceN.gestionpatient.securite.service.JetonActivationService;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.PatientInscriptionDto;
import com.forceN.gestionpatient.utilisateur.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final JetonActivationService jetonActivationService;

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

    // Protégée : il faut déjà être connecté. Le personnel choisit (ou change) l'établissement où il travaille,
    // et reçoit un nouveau jeton qui le désigne.
    @PostMapping("/etablissement")
    public ConnexionReponse choisirEtablissement(@AuthenticationPrincipal CompteAuthentifie connecte,
            @Valid @RequestBody ChoixEtablissementRequete requete) {
        return authService.choisirEtablissement(connecte, requete.etablissementId());
    }

    // Route publique : la personne n'a pas encore de mot de passe, donc pas de jeton de connexion.
    // C'est le jeton d'activation reçu par email qui prouve son identité.
    @PostMapping("/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activer(@Valid @RequestBody ChoixMotDePasseRequete requete) {
        jetonActivationService.activer(requete.getJeton(), requete.getMotDePasse());
    }

    // Publique, et toujours 204 : la réponse ne révèle pas si l'email a un compte.
    @PostMapping("/mot-de-passe-oublie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void motDePasseOublie(@Valid @RequestBody MotDePasseOublieRequete requete) {
        jetonActivationService.demanderReinitialisation(requete.getEmail());
    }

    // Publique : la personne a oublié son mot de passe, c'est le jeton de l'email qui prouve son identité.
    @PostMapping("/reinitialisation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reinitialiser(@Valid @RequestBody ChoixMotDePasseRequete requete) {
        jetonActivationService.reinitialiser(requete.getJeton(), requete.getMotDePasse());
    }

}
