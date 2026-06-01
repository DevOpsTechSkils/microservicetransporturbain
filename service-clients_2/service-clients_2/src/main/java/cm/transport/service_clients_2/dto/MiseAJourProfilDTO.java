package cm.transport.service_clients_2.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données modifiables du profil client")
public class MiseAJourProfilDTO {

    @Schema(description = "Nouveau nom complet", example = "Tchamdjou Paul Junior")
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, max = 100)
    private String nomComplet;

    @Schema(description = "Nouveau numéro de téléphone", example = "699887766")
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^(\\+237|237)?[6-9][0-9]{8}$",
            message = "Format téléphone camerounais invalide")
    private String telephone;

    @Schema(description = "Nouvelle ville", example = "Yaoundé")
    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @Schema(description = "Numéro de CNI", example = "CM009988776")
    private String numeroCNI;

    @Schema(description = "URL de la photo de profil")
    private String photoUrl;
}

