package cm.transport.service_clients_2.config;

// Configuration de la documentation API

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//  Accès : http://localhost:8081/swagger-ui.html                   ║
 //  JSON   : http://localhost:8081/v3/api-docs                      ║
@Configuration
// @SecurityScheme = définit comment le JWT est transmis dans Swagger UI
// Quand on clique sur "Authorize" dans Swagger, on colle le token JWT
// Swagger l'enverra automatiquement dans le header Authorization: Bearer xxx
@SecurityScheme(
        name = "bearerAuth",                        // Nom du schéma (référencé dans @SecurityRequirement)
        type = SecuritySchemeType.HTTP,             // Type : HTTP (pas API Key)
        bearerFormat = "JWT",                       // Format : JWT
        scheme = "bearer",                          // Schéma HTTP : bearer
        description = "Coller le token JWT obtenu après /api/auth/login. Format : eyJhbGci..."
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Clients — Transport Interurbain Cameroun")
                        .description("""
                    ## API de gestion des clients voyageurs
                    
                    **Développeur :** Dev2
                    
                    ### Fonctionnalités :
                    -  Inscription avec validation des données
                    -  Connexion et génération de token JWT
                    -  Gestion du profil personnel
                    -  Changement de mot de passe sécurisé
                    - ️ Administration des comptes (ROLE_ADMIN)
                    
                    ### Comment utiliser l'authentification :
                    1. Créer un compte via `POST /api/auth/inscription`
                    2. Se connecter via `POST /api/auth/login`
                    3. Copier le token `accessToken` de la réponse
                    4. Cliquer sur le bouton **Authorize 🔒** en haut
                    5. Coller le token et valider
                    6. Tous les endpoints protégés fonctionneront automatiquement
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dev2 — Équipe Transport Cameroun")
                                .email("dev2@transport-cameroun.cm"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("🖥 Serveur de développement (direct)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description(" Via API Gateway (recommandé)")
                ));
    }
}
