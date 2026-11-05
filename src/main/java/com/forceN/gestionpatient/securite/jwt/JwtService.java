package com.forceN.gestionpatient.securite.jwt;

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

    public String genererJeton(String email) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(duree)))
                .signWith(cle)
                .compact();
    }

    /** Renvoie l'email contenu dans le jeton, ou rien si le jeton est faux ou expiré. */
    public Optional<String> extraireEmail(String jeton) {
        try {
            return Optional.ofNullable(Jwts.parser().verifyWith(cle).build()
                    .parseSignedClaims(jeton).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
