package com.forceN.gestionpatient.securite.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey cle;
    private final Duration duree;

    public JwtService(@Value("${securite.jwt.secret}") String secret,
                      @Value("${securite.jwt.duree-minutes}") long dureeMinutes) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.duree = Duration.ofMinutes(dureeMinutes);
    }

    // Nom de l'information « établissement » dans le jeton.
    private static final String ETABLISSEMENT = "etablissement";

    /** Jeton sans établissement : pour un patient, ou pour le personnel qui doit encore choisir le sien. */
    public String genererJeton(String email) {
        return genererJeton(email, null);
    }

    /**
     * Jeton qui désigne l'établissement où la personne travaille. Il ne contient ni rôle ni permission :
     * ils sont relus en base à chaque requête, pour qu'un accès retiré le soit tout de suite.
     */
    public String genererJeton(String email, Integer etablissementId) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim(ETABLISSEMENT, etablissementId) // une valeur null n'est pas écrite dans le jeton
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(duree)))
                .signWith(cle)
                .compact();
    }

    /** Renvoie le contenu du jeton, ou rien si le jeton est faux, falsifié ou expiré. */
    public Optional<JetonLu> lire(String jeton) {
        try {
            Claims contenu = Jwts.parser().verifyWith(cle).build().parseSignedClaims(jeton).getPayload();
            if (contenu.getSubject() == null || contenu.getIssuedAt() == null) {
                return Optional.empty();
            }
            return Optional.of(new JetonLu(contenu.getSubject(), contenu.getIssuedAt().toInstant(),
                    contenu.get(ETABLISSEMENT, Integer.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            // Un établissement d'un mauvais type (RequiredTypeException) est aussi un JwtException.
            return Optional.empty();
        }
    }
}
