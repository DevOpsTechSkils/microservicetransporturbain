package cm.transport.service_clients_2.dto;


import cm.transport.service_clients_2.model.Client;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Profil complet d'un client voyageur")
public class ProfilDTO {

    @Schema(description = "Identifiant unique du client", example = "1")
    private Long id;

    @Schema(description = "Nom complet", example = "Tchamdjou Paul")
    private String nomComplet;

    @Schema(description = "Adresse email", example = "paul@gmail.com")
    private String email;

    @Schema(description = "Numéro de téléphone", example = "677123456")
    private String telephone;

    @Schema(description = "Ville de résidence", example = "Douala")
    private String ville;

    @Schema(description = "Numéro de CNI", example = "CM001234567")
    private String numeroCNI;

    @Schema(description = "URL de la photo de profil")
    private String photoUrl;

    @Schema(description = "Rôle dans l'application", example = "ROLE_CLIENT")
    private String role;

    @Schema(description = "Compte actif ou non", example = "true")
    private boolean actif;

    @Schema(description = "Email vérifié ou non", example = "false")
    private boolean emailVerifie;

    @Schema(description = "Date d'inscription", example = "2025-01-15T09:30:00")
    private LocalDateTime dateInscription;

    @Schema(description = "Dernière connexion", example = "2025-01-20T14:45:00")
    private LocalDateTime derniereConnexion;

    /**
     * MÉTHODE STATIQUE DE CONVERSION : Entité → DTO
     *
     * Cette méthode "factory" crée un ProfilDTO depuis une entité Client.
     * On centralise la conversion ici pour éviter de répéter ce code partout.
     *
     * Principe : le DTO ne doit pas "savoir" comment fonctionne l'entité,
     * mais une méthode utilitaire de conversion est acceptable.
     *
     * @param client L'entité Client de la base de données
     * @return Un ProfilDTO sans le mot de passe
     */
    public static ProfilDTO depuisClient(Client client) {
        return ProfilDTO.builder()
                .id(client.getId())
                .nomComplet(client.getNomComplet())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .ville(client.getVille())
                .numeroCNI(client.getNumeroCNI())
                .photoUrl(client.getPhotoUrl())
                // role.name() retourne "ROLE_CLIENT" ou "ROLE_ADMIN"
                .role(client.getRole().name())
                .actif(client.isActif())
                .emailVerifie(client.isEmailVerifie())
                .dateInscription(client.getDateInscription())
                .derniereConnexion(client.getDerniereConnexion())
                .build();
    }
}
