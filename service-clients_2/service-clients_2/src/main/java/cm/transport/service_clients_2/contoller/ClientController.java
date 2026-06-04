package cm.transport.service_clients_2.contoller;

import cm.transport.service_clients_2.dto.ChangementMotDePasseDTO;
import cm.transport.service_clients_2.dto.MessageReponseDTO;
import cm.transport.service_clients_2.dto.MiseAJourProfilDTO;
import cm.transport.service_clients_2.dto.ProfilDTO;
import cm.transport.service_clients_2.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
// @SecurityRequirement = indique dans Swagger que ces endpoints nécessitent un JWT
// "bearerAuth" correspond au nom du schéma de sécurité défini dans SwaggerConfig
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = " Profils Clients",
        description = "Gestion des profils voyageurs. Token JWT obligatoire dans le header."
)
public class ClientController {

    private final ClientService clientService;

    // GET /api/clients/profil/moi — Voir son propre profil

    @Operation(
            summary = "Consulter mon profil",
            description = "Retourne le profil complet du client actuellement connecté (identifié via le token JWT)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Profil retourné",
                    content = @Content(schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "401", description = " Token absent ou invalide"),
            @ApiResponse(responseCode = "403", description = " Token expiré")
    })
    @GetMapping("/profil/moi")
    public ResponseEntity<ProfilDTO> getMonProfil() {
        log.info("GET /api/clients/profil/moi");
        ProfilDTO profil = clientService.getMonProfil();
        return ResponseEntity.ok(profil);
    }

   // PUT /api/clients/profil/moi — Modifier son profil

    @Operation(
            summary = "Modifier mon profil",
            description = """
            Met à jour les informations personnelles du client connecté.
            
            **Champs modifiables : nom, téléphone, ville, CNI, photo
            
            **Non modifiables ici : email (identifiant), mot de passe (endpoint dédié)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Profil mis à jour",
                    content = @Content(schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "400", description = " Données invalides"),
            @ApiResponse(responseCode = "401", description = " Non authentifié"),
            @ApiResponse(responseCode = "409", description = " Téléphone déjà utilisé")
    })
    @PutMapping("/profil/moi")
    public ResponseEntity<ProfilDTO> mettreAJourMonProfil(
            @Valid @RequestBody MiseAJourProfilDTO dto
    ) {
        log.info("PUT /api/clients/profil/moi");
        ProfilDTO profilMisAJour = clientService.mettreAJourMonProfil(dto);
        return ResponseEntity.ok(profilMisAJour);
    }

    // PUT /api/clients/profil/changer-mot-de-passe
    @Operation(
            summary = "Changer mon mot de passe",
            description = """
            Change le mot de passe du client connecté.
            Nécessite de fournir l'ancien mot de passe pour confirmation.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Mot de passe changé"),
            @ApiResponse(responseCode = "400", description = " Ancien mot de passe incorrect ou nouveau invalide"),
            @ApiResponse(responseCode = "401", description = " Non authentifié")
    })
    @PutMapping("/profil/changer-mot-de-passe")
    public ResponseEntity<MessageReponseDTO> changerMotDePasse(
            @Valid @RequestBody ChangementMotDePasseDTO dto
    ) {
        log.info("PUT /api/clients/profil/changer-mot-de-passe");
        clientService.changerMotDePasse(dto);
        return ResponseEntity.ok(MessageReponseDTO.succes(
                "Mot de passe modifié avec succès"
        ));
    }


    // GET /api/clients/{id} — Voir un profil par ID

    @Operation(
            summary = "Consulter un profil par ID",
            description = "Retourne le profil d'un client par son ID. Accessible aux admins et au client lui-même."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Profil trouvé",
                    content = @Content(schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "401", description = " Non authentifié"),
            @ApiResponse(responseCode = "404", description = " Client introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProfilDTO> getClientParId(
            // @Parameter = documente le paramètre dans Swagger
            @Parameter(description = "Identifiant unique du client", example = "1")
            @PathVariable Long id
            // @PathVariable = extrait {id} depuis l'URL (/api/clients/1 -> id=1)
    ) {
        log.info("GET /api/clients/{}", id);
        ProfilDTO profil = clientService.getClientParId(id);
        return ResponseEntity.ok(profil);
    }
}


@Slf4j
@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = " Administration Clients",
        description = "Endpoints d'administration. RÉSERVÉ aux admins (ROLE_ADMIN)."
)
class AdminClientController {

    private final ClientService clientService;

    // GET /api/admin/clients — Lister tous les clients (paginé)

    @Operation(
            summary = "Lister tous les clients (avec pagination)",
            description = """
            Retourne la liste paginée de tous les clients actifs.
            
            **Paramètres de pagination :**
            - `page` : numéro de page (commence à 0)
            - `size` : nombre d'éléments par page (défaut: 20)
            - `sort` : champ de tri (ex: sort=nomComplet,asc)
            
            **Exemple :** `/api/admin/clients?page=0&size=10&sort=dateInscription,desc`
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Page de clients retournée"),
            @ApiResponse(responseCode = "401", description = " Non authentifié"),
            @ApiResponse(responseCode = "403", description = " Accès refusé (pas ROLE_ADMIN)")
    })
    @GetMapping
    public ResponseEntity<Page<ProfilDTO>> listerTousLesClients(
            // @PageableDefault = valeurs par défaut de la pagination
            // page=0 -> première page, size=20 -> 20 résultats par page
            // sort = trier par dateInscription décroissant (plus récents d'abord)
            @PageableDefault(page = 0, size = 20,
                    sort = "dateInscription",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/admin/clients — page={}", pageable.getPageNumber());
        Page<ProfilDTO> clients = clientService.listerTousLesClients(pageable);
        return ResponseEntity.ok(clients);
    }


    // GET /api/admin/clients/recherche?terme=paul&page=0&size=10

    @Operation(
            summary = "Rechercher des clients",
            description = "Recherche par nom, email ou téléphone (insensible à la casse)."
    )
    @GetMapping("/recherche")
    public ResponseEntity<Page<ProfilDTO>> rechercherClients(
            // @RequestParam = paramètre de requête dans l'URL (?terme=paul)
            @Parameter(description = "Terme de recherche", example = "paul")
            @RequestParam String terme,

            @PageableDefault(size = 10)
            Pageable pageable
    ) {
        log.info("GET /api/admin/clients/recherche?terme={}", terme);
        Page<ProfilDTO> resultats = clientService.rechercherClients(terme, pageable);
        return ResponseEntity.ok(resultats);
    }


    // DELETE /api/admin/clients/{id} — Désactiver un client


    @Operation(
            summary = "Désactiver un client",
            description = """
            Désactive le compte d'un client (soft delete).
            Le client ne pourra plus se connecter mais ses données sont conservées.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = " Client désactivé"),
            @ApiResponse(responseCode = "403", description = " Accès refusé"),
            @ApiResponse(responseCode = "404", description = " Client introuvable")
    })
    // @DeleteMapping = écoute les requêtes HTTP DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageReponseDTO> desactiverClient(
            @Parameter(description = "ID du client à désactiver", example = "5")
            @PathVariable Long id
    ) {
        log.warn("DELETE /api/admin/clients/{} — Désactivation par admin", id);
        clientService.desactiverClient(id);
        return ResponseEntity.ok(MessageReponseDTO.succes(
                "Client ID " + id + " désactivé avec succès"
        ));
    }


    // GET /api/admin/clients/statistiques — Dashboard admin

    @Operation(
            summary = "Statistiques des clients",
            description = "Retourne les statistiques globales : total actifs, répartition par ville, etc."
    )
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        log.info("GET /api/admin/clients/statistiques");
        Map<String, Object> stats = clientService.getStatistiques();
        return ResponseEntity.ok(stats);
    }
}
