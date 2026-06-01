package cm.transport.service_clients_2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 3. DTO RÉPONSE AUTH — Données retournées après connexion ou inscription
// Contient le token JWT que le client devra inclure dans ses requêtes suivantes
// ═══════════════════════════════════════════════════════════════════
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse d'authentification contenant le token JWT")
public class AuthReponseDTO {

    @Schema(description = "Token JWT d'accès (à inclure dans le header Authorization: Bearer <token>)",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwYXVsQGdtYWlsLmNvbSJ9.xxx")
    private String accessToken;

    @Schema(description = "Type du token (toujours 'Bearer')", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité du token en secondes", example = "86400")
    private long expiresDans;

    @Schema(description = "Informations du client connecté")
    private ProfilDTO client;
}
