package cm.transport.service_clients_2.model;


// ══════════════════════════════════════════════════════════════════
// IMPORTS — On importe les classes dont on a besoin
// ══════════════════════════════════════════════════════════════════

// Annotations JPA pour le mapping Objet-Relationnel
import jakarta.persistence.*;
// Annotations de validation des données
import jakarta.validation.constraints.*;
// Annotations Lombok pour générer le code répétitif
import lombok.*;
// Spring Security : interface que notre entité doit implémenter
// pour être reconnue comme un "utilisateur" par Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Data
// @Builder = permet la syntaxe : Client.builder().email("x").build()
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "clients",
        // Contraintes d'unicité en base de données (niveau BDD, pas application)
        uniqueConstraints = {
                // L'email doit être unique → empêche deux comptes avec le même email
                @UniqueConstraint(columnNames = "email", name = "uk_client_email"),
                // Le téléphone doit être unique → un numéro = un compte
                @UniqueConstraint(columnNames = "telephone", name = "uk_client_telephone")
        }
)
public class Client implements UserDetails {
    // UserDetails est une INTERFACE de Spring Security
    // En l'implémentant, notre classe Client devient un "utilisateur"
    // reconnu par Spring Security pour l'authentification

    @Id
    // GenerationType.IDENTITY = AUTO_INCREMENT géré par la base (1, 2, 3...)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INFORMATIONS PERSONNELLES

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit avoir entre 3 et 100 caractères")
    private String nomComplet;

    //Email = identifiant de connexion (login), Doit être unique dans toute la base
    @Column(nullable = false, unique = true, length = 150)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide (exemple : paul@gmail.com)")
    private String email;

    //Mot de passe hashé (JAMAIS en clair !)
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    //Numéro de téléphone camerounais
     // @Pattern = vérifie le format avec une expression régulière
     // Format : 677123456 ou +237677123456
    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(
            regexp = "^(\\+237|237)?[6-9][0-9]{8}$",
            message = "Format téléphone camerounais invalide (ex: 677123456 ou +237677123456)"
    )
    private String telephone;

    //Ville de résidence du voyageur
     // Utilisée pour afficher les trajets disponibles depuis sa ville

    @Column(nullable = false, length = 50)
    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    //Numéro de Carte Nationale d'Identité (optionnel)
    @Column(length = 20)
    private String numeroCNI;

    // URL de la photo de profil (optionnel)
     // Ex: "https://storage.transcam.cm/photos/user_1.jpg"
    @Column(length = 500)
    private String photoUrl;

    // SÉCURITÉ ET AUTORISATION

    //Role de l'utilisateur (ROLE_CLIENT ou ROLE_ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default    // Valeur par défaut quand on utilise le Builder
    private Role role = Role.ROLE_CLIENT;

     //Compte actif ou non
     // Un compte désactivé ne peut plus se connecter
     // @Builder.Default = valeur par défaut dans le Builder : true (actif)
    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    // Compte vérifié par email ou non
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerifie = false;

    // DATES
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    //Dernière connexion (mise à jour à chaque login)
    @Column
    private LocalDateTime derniereConnexion;

    //Date de dernière modification du profil
    @Column
    private LocalDateTime dateMiseAJour;

    // MÉTHODES JPA AUTOMATIQUES

    //@PrePersist = méthode appelée automatiquement par JPA AVANT la première sauvegarde en base (INSERT),Initialise les champs calculés automatiquement

    @PrePersist
    protected void avantCreation() {
        // Initialise la date d'inscription avec la date/heure actuelle
        this.dateInscription = LocalDateTime.now();
    }

    @PreUpdate
    protected void avantMiseAJour() {
        // Met à jour la date de modification
        this.dateMiseAJour = LocalDateTime.now();
    }

    // ─── MÉTHODES SPRING SECURITY (interface UserDetails) ────────
    // Spring Security appelle ces méthodes pour gérer l'authentification

    /**
     * getAuthorities() — Retourne les droits/permissions de l'utilisateur
     * Spring Security utilise cette méthode pour les vérifications d'accès
     *
     * Un "GrantedAuthority" = un droit accordé (ex: "ROLE_CLIENT")
     * SimpleGrantedAuthority = implémentation simple d'une autorité
     *
     * @return Collection de permissions (ici une seule : le rôle)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // On retourne le rôle du client sous forme d'autorité Spring Security
        // role.name() retourne "ROLE_CLIENT" ou "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    //getPassword() — Retourne le mot de passe hashé

    @Override
    public String getPassword() {
        return motDePasse;
    }

    //getUsername() — Retourne l'identifiant de connexion
    //on utilise l'email comme identifiant (au lieu d'un username)
    @Override
    public String getUsername() {
        return email; // L'email est notre identifiant de connexion
    }

    //isAccountNonExpired() — Le compte n'est-il pas expiré ?
    @Override
    public boolean isAccountNonExpired() {
        return true; // Nos comptes n'expirent pas
    }

    /**
     * isAccountNonLocked() — Le compte n'est-il pas verrouillé ?
     * On retourne la valeur de "actif" :
     *   - Si actif = true  → compte non verrouillé -. connexion autorisée
     *   - Si actif = false → compte verrouillé    -> connexion refusée
     */
    @Override
    public boolean isAccountNonLocked() {
        return actif; // Un compte inactif est considéré comme verrouillé
    }


     // isCredentialsNonExpired() — Les identifiants ne sont-ils pas expirés ?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    //isEnabled() — Le compte est-il activé ?
    @Override
    public boolean isEnabled() {
        return actif;
    }

    //  ÉNUMÉRATION DES ROLES

    // Rôles possibles dans l'application
    public enum Role {
        ROLE_CLIENT,    // Voyageur standard (peut réserver des billets)
        ROLE_ADMIN      // Administrateur (peut gérer tous les clients)
    }
}