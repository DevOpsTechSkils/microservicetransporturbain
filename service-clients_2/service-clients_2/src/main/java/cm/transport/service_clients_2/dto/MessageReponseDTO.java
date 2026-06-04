package cm.transport.service_clients_2.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 7. DTO MESSAGE GÉNÉRIQUE — Réponse simple avec un message
// Utilisé pour les confirmations (inscription réussie, etc.)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Message de réponse simple")
public class MessageReponseDTO {

    @Schema(description = "Message informatif", example = "Inscription réussie !")
    private String message;

    @Schema(description = "Code de statut", example = "SUCCESS")
    private String statut;

    @Schema(description = "Timestamp de la réponse")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Méthode de création rapide pour les messages de succès
    public static MessageReponseDTO succes(String message) {
        return MessageReponseDTO.builder()
                .message(message)
                .statut("SUCCESS")
                .build();
    }

    // Méthode de création rapide pour les messages d'erreur
    public static MessageReponseDTO erreur(String message) {
        return MessageReponseDTO.builder()
                .message(message)
                .statut("ERROR")
                .build();
    }
}

