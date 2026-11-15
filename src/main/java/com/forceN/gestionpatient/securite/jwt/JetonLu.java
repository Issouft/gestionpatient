package com.forceN.gestionpatient.securite.jwt;

import java.time.Instant;

/**
 * Ce que l'on retient d'un jeton de connexion valide : à qui il appartient, quand il a été créé,
 * et dans quel établissement la personne travaille (null pour un patient, ou tant que le personnel n'a pas choisi).
 * La date sert à refuser les jetons créés avant un changement de mot de passe.
 * Le jeton ne contient ni le rôle ni les permissions : ils sont relus en base à chaque requête.
 */
public record JetonLu(String email, Instant emisLe, Integer etablissementId) {
}
