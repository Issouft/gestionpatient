package com.forceN.gestionpatient.securite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Envoie les emails de l'application, via le compte Gmail configuré dans .env. */
@Service
@RequiredArgsConstructor
public class EmailService {

    // Fourni par Spring grâce à spring-boot-starter-mail et aux réglages spring.mail.*
    private final JavaMailSender mailSender;

    // @Value lit une valeur de la configuration (application.properties ou .env).
    // Gmail exige que l'expéditeur soit le compte connecté.
    @Value("${spring.mail.username}")
    private String expediteur;

    @Value("${application.url-activation}")
    private String urlActivation;

    /** Envoie le lien d'invitation au nouveau membre du personnel. */
    public void envoyerInvitation(String destinataire, String prenom, String jeton) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire);
        message.setSubject("Activez votre compte gestionpatient");
        message.setText("Bonjour " + prenom + ",\n\n"
                + "Un compte a été créé pour vous. Choisissez votre mot de passe en ouvrant ce lien :\n"
                + urlActivation + jeton + "\n\n"
                + "Ce lien est valable 48 heures et ne fonctionne qu'une seule fois.");
        mailSender.send(message);
    }
}
