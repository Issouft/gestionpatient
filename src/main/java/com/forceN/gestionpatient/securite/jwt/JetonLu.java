package com.forceN.gestionpatient.securite.jwt;

import java.time.Instant;

/**
 * Ce que l'on retient d'un jeton de connexion valide : à qui il appartient et quand il a été créé.
 * La date sert à refuser les jetons créés avant un changement de mot de passe.
 */
public record JetonLu(String email, Instant emisLe) {
}
