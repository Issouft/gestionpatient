package com.forceN.gestionpatient.securite.config;

import com.forceN.gestionpatient.securite.exception.SecuriteErreurHandler;
import com.forceN.gestionpatient.securite.jwt.JwtFiltre;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecuriteErreurHandler securiteErreurHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Pas de session ni de cookie : le jeton est envoyé à chaque requête.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Tout est fermé par défaut : une route publique doit être ajoutée ici.
                .authorizeHttpRequests(routes -> routes
                        .requestMatchers(HttpMethod.POST, "/auth/inscription").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/connexion").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint(securiteErreurHandler)
                        .accessDeniedHandler(securiteErreurHandler))
                // Créé ici et non comme @Component, pour que le filtre ne s'exécute que dans cette chaîne.
                .addFilterBefore(new JwtFiltre(jwtService, userDetailsService), UsernamePasswordAuthenticationFilter.class)
                .build();
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
