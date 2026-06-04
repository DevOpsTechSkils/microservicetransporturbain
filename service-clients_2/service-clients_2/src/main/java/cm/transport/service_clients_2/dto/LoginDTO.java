package cm.transport.service_clients_2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 2. DTO LOGIN — Données reçues lors de la connexion
// POST /api/auth/login
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Identifiants de connexion")
public class LoginDTO {

    @Schema(description = "Email du compte", example = "paul@gmail.com")
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Schema(description = "Mot de passe du compte", example = "Paul@2024")
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}

