package com.forceN.gestionpatient.commun.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Configuration de la page Swagger (/swagger-ui.html), qui sert à tester l'API depuis le navigateur.
 * - Le bouton « Authorize » reçoit le jeton de POST /auth/connexion : il est ensuite envoyé avec chaque requête.
 * - Les routes sont regroupées par partie selon le début de leur chemin (voir GROUPES).
 * Ne change rien à la sécurité : une route protégée répond toujours 401 sans jeton.
 */
@Configuration
public class SwaggerConfig {

    private static final String JETON = "jeton";

    /**
     * Début du chemin → groupe affiché dans Swagger. Une nouvelle partie (ex. /rendez-vous) s'ajoute ici.
     * Une route qui ne correspond à aucun groupe reste affichée, sous le nom de son contrôleur.
     */
    private static final Map<String, Tag> GROUPES = groupes(
            "/auth", "Authentification", "Inscription, connexion, choix de l'établissement, activation et mot de passe oublié",
            "/utilisateurs", "Mon compte", "Profil et mot de passe de la personne connectée",
            "/etablissements", "Établissements", "Réservé au super-administrateur : établissements et invitation de leur administrateur",
            "/personnel", "Personnel", "Réservé à l'administrateur, dans son établissement",
            "/dossiers", "Dossiers patients", "Partie 3",
            "/consultations", "Consultations", "Partie 3",
            "/ordonnances", "Ordonnances", "Partie 3",
            "/medicaments", "Médicaments", "Partie 3",
            "/demandes", "Demandes de disponibilité", "Partie 3",
            "/rapports", "Rapports", "Partie 3");

    @Bean
    public OpenAPI documentationApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API gestionpatient")
                        .version("1")
                        .description("""
                                Pour tester une route protégée : 1. POST /auth/connexion ; 2. copier le champ "jeton" \
                                de la réponse ; 3. cliquer sur « Authorize » et le coller (sans écrire Bearer). \
                                Les erreurs ont toujours un champ "code" (voir le README, « Si quelque chose ne marche pas »)."""))
                // Le jeton est demandé pour toutes les routes ; les routes publiques l'ignorent.
                .components(new Components().addSecuritySchemes(JETON, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JETON))
                .tags(List.copyOf(GROUPES.values()));
    }

    /** Range chaque route dans le groupe qui correspond au début de son chemin. */
    @Bean
    public OpenApiCustomizer rangerLesRoutesParPartie() {
        return openApi -> openApi.getPaths().forEach((chemin, route) -> GROUPES.forEach((debut, groupe) -> {
            if (chemin.equals(debut) || chemin.startsWith(debut + "/")) {
                route.readOperations().forEach(operation -> operation.setTags(List.of(groupe.getName())));
            }
        }));
    }

    /** Construit GROUPES à partir de triplets (début du chemin, nom, description), en gardant l'ordre. */
    private static Map<String, Tag> groupes(String... triplets) {
        Map<String, Tag> groupes = new LinkedHashMap<>();
        for (int i = 0; i < triplets.length; i += 3) {
            groupes.put(triplets[i], new Tag().name(triplets[i + 1]).description(triplets[i + 2]));
        }
        return groupes;
    }
}
