package cm.transport.service_clients_2.contoller;


import cm.transport.service_clients_2.dto.AuthReponseDTO;
import cm.transport.service_clients_2.dto.InscriptionDTO;
import cm.transport.service_clients_2.dto.LoginDTO;
import cm.transport.service_clients_2.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController

@RequestMapping("/api/auth")

@RequiredArgsConstructor

// @Tag = annotation Swagger pour grouper les endpoints dans la documentation
@Tag(
        name = " Authentification",
        description = "Endpoints d'inscription et de connexion. Aucun token requis."
)
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/inscription — Créer un compte voyageur

    // Inscrit un nouveau client voyageur.
    @Operation(
            summary = "Inscription d'un nouveau client voyageur",
            description = """
            Crée un nouveau compte client et retourne immédiatement un token JWT.
            
            **Règles du mot de passe :**
            - Minimum 8 caractères
            - Au moins une majuscule
            - Au moins un chiffre
            - Au moins un caractère spécial (!@#$%^&*)
            
            **Format téléphone camerounais :**
            - 677123456 (9 chiffres)
            - +237677123456 (avec indicatif)
            """
    )
    // @ApiResponses = documente les différents codes HTTP possibles dans Swagger
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = " Inscription réussie — Token JWT retourné",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthReponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = " Données invalides (email mal formé, mot de passe trop faible...)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = " Email ou téléphone déjà utilisé par un autre compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    // @PostMapping = écoute les requêtes HTTP POST sur /api/auth/inscription
    @PostMapping("/inscription")
    public ResponseEntity<AuthReponseDTO> inscrire(
            @Valid @RequestBody InscriptionDTO dto
    ) {
        log.info("POST /api/auth/inscription — {}", dto.getEmail());
        AuthReponseDTO reponse = authService.inscrire(dto);
        // HttpStatus.CREATED = code HTTP 201 (convention REST pour une création réussie)
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    // POST /api/auth/login — Se connecter

    @Operation(
            summary = "Connexion d'un client existant",
            description = """
            Vérifie les identifiants et retourne un token JWT valide 24h.
            
            **Utilisation du token :**
            Dans toutes les requêtes suivantes, ajouter le header :
            ```
            Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
            ```
            
            **Dans Swagger UI :** Cliquer sur  Authorize et coller le token.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = " Connexion réussie — Token JWT retourné",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthReponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemple de réponse",
                                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                          "tokenType": "Bearer",
                          "expiresDans": 86400,
                          "client": {
                            "id": 1,
                            "nomComplet": "Tchamdjou Paul",
                            "email": "paul@gmail.com",
                            "role": "ROLE_CLIENT"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = " Email ou mot de passe incorrect"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = " Compte désactivé"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthReponseDTO> connecter(
            @Valid @RequestBody LoginDTO dto
    ) {
        log.info("POST /api/auth/login — {}", dto.getEmail());
        AuthReponseDTO reponse = authService.connecter(dto);
        return ResponseEntity.ok(reponse);
    }
}
