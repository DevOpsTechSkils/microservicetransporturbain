package cm.transport.service_clients_2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 8. DTO ERREUR API — Format standard des erreurs retournées par l'API
// Utilisé par le GlobalExceptionHandler

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Détails d'une erreur API")
public class ErreurApiDTO {

    @Schema(description = "Horodatage de l'erreur")
    private LocalDateTime timestamp;

    @Schema(description = "Code HTTP", example = "404")
    private int statut;

    @Schema(description = "Type d'erreur", example = "Not Found")
    private String erreur;

    @Schema(description = "Message détaillé", example = "Aucun client trouvé avec l'ID : 999")
    private String message;

    @Schema(description = "Endpoint appelé", example = "/api/clients/999")
    private String chemin;

    @Schema(description = "Détails de validation (champ → message d'erreur)")
    private java.util.Map<String, String> details;
}

