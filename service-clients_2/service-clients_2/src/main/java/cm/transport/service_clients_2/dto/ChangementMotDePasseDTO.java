package cm.transport.service_clients_2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 6. DTO CHANGEMENT DE MOT DE PASSE
// PUT /api/clients/profil/changer-mot-de-passe
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données pour changer le mot de passe")
public class ChangementMotDePasseDTO {

    @Schema(description = "Mot de passe actuel", example = "Paul@2024")
    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String motDePasseActuel;

    @Schema(description = "Nouveau mot de passe", example = "NouveauPaul@2025")
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Le nouveau mot de passe ne respecte pas les critères de sécurité"
    )
    private String nouveauMotDePasse;

    @Schema(description = "Confirmation du nouveau mot de passe", example = "NouveauPaul@2025")
    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmationNouveauMotDePasse;
}

