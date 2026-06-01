package cm.transport.service_clients_2.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;


// ═══════════════════════════════════════════════════════════════════
// 1. DTO INSCRIPTION — Données reçues lors de la création d'un compte
// POST /api/auth/inscription
// ═══════════════════════════════════════════════════════════════════
@Data                // Lombok : getters, setters, equals, hashCode, toString
@NoArgsConstructor   // Constructeur vide (requis pour la désérialisation JSON)
@AllArgsConstructor  // Constructeur avec tous les champs
@Builder             // Pattern Builder pour les tests
// @Schema = annotation Swagger pour documenter le DTO dans Swagger UI
@Schema(description = "Données nécessaires pour créer un compte client voyageur")
public class InscriptionDTO {

    // @Schema(example = "...") = valeur d'exemple affichée dans Swagger UI
    @Schema(description = "Nom complet du voyageur", example = "Tchamdjou Paul")
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit avoir entre 3 et 100 caractères")
    private String nomComplet;

    @Schema(description = "Adresse email (sera utilisée comme identifiant)", example = "paul@gmail.com")
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Schema(description = "Mot de passe (minimum 8 caractères, doit contenir majuscule, chiffre et caractère spécial)", example = "Paul@2024")
    @NotBlank(message = "Le mot de passe est obligatoire")
    // @Pattern avec regex pour exiger un mot de passe fort :
    // (?=.*[A-Z]) → au moins une MAJUSCULE
    // (?=.*[0-9]) → au moins un CHIFFRE
    // (?=.*[!@#$%^&*]) → au moins un CARACTÈRE SPÉCIAL
    // .{8,} → minimum 8 CARACTÈRES
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Le mot de passe doit contenir au minimum 8 caractères, une majuscule, un chiffre et un caractère spécial (!@#$%^&*)"
    )
    private String motDePasse;

    @Schema(description = "Confirmation du mot de passe (doit correspondre au mot de passe)", example = "Paul@2024")
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmationMotDePasse;

    @Schema(description = "Numéro de téléphone camerounais", example = "677123456")
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(
            regexp = "^(\\+237|237)?[6-9][0-9]{8}$",
            message = "Numéro camerounais invalide (ex: 677123456 ou +237677123456)"
    )
    private String telephone;

    @Schema(description = "Ville de résidence", example = "Douala")
    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @Schema(description = "Numéro de CNI (optionnel)", example = "CM001234567")
    private String numeroCNI;
}