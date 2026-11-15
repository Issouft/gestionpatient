package com.forceN.gestionpatient.securite.config;

import com.forceN.gestionpatient.securite.exception.SecuriteErreurHandler;
import com.forceN.gestionpatient.securite.jwt.JwtFiltre;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.securite.service.UtilisateurConnecteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UtilisateurConnecteService utilisateurConnecteService;
    private final SecuriteErreurHandler securiteErreurHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Pas de session ni de cookie : le jeton est envoyé à chaque requête.
                .csrf(AbstractHttpConfigurer::disable)
                // Le frontend (autre adresse que l'API) a le droit d'appeler l'API : voir corsConfigurationSource().
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Tout est fermé par défaut : une route publique doit être ajoutée ici.
                .authorizeHttpRequests(routes -> routes
                        .requestMatchers(HttpMethod.POST, "/auth/inscription").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/connexion").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/activation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/mot-de-passe-oublie").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reinitialisation").permitAll()
                        // Annuaire public : un patient choisit sa clinique et son médecin avant d'avoir un compte.
                        // Lecture seule, et seulement des fiches publiques (voir EtablissementPublicDto, MedecinPublicDto).
                        .requestMatchers(HttpMethod.GET, "/annuaire/**").permitAll()
                        // Swagger : seulement la page et la description des routes (lecture).
                        // Les routes elles-mêmes restent protégées. Désactivable avec SWAGGER_ACTIF=false.
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated())
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint(securiteErreurHandler)
                        .accessDeniedHandler(securiteErreurHandler))
                // Créé ici et non comme @Component, pour que le filtre ne s'exécute que dans cette chaîne.
                .addFilterBefore(new JwtFiltre(jwtService, utilisateurConnecteService),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * CORS : le navigateur bloque une page de localhost:4200 qui appelle l'API sur localhost:8081 (deux « origines »),
     * sauf si l'API l'autorise. On autorise seulement l'adresse du frontend (application.url-frontend), pas « * ».
     * Pas de cookies : le jeton voyage dans l'en-tête Authorization.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${application.url-frontend}") String urlFrontend) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(urlFrontend));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L); // le navigateur garde la réponse 1 h au lieu de redemander avant chaque requête
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
