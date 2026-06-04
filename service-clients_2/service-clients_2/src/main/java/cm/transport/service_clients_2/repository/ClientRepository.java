package cm.transport.service_clients_2.repository;

import cm.transport.service_clients_2.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Trouve un client par son email.
    Optional<Client> findByEmail(String email);

    // Vérifie si un email est déjà utilisé.
    boolean existsByEmail(String email);

    //Vérifie si un numéro de téléphone est déjà utilisé.
    boolean existsByTelephone(String telephone);

    //Trouve un client par son numéro de téléphone.
    Optional<Client> findByTelephone(String telephone);


     // Liste tous les clients actifs avec PAGINATION.
     // Pageable = permet de récupérer par pages (ex: page 0, 10 résultats)

    Page<Client> findByActifTrue(Pageable pageable);

    //Liste les clients d'une ville spécifique (actifs uniquement).
    List<Client> findByVilleAndActifTrue(String ville);

    //Liste tous les clients avec un rôle donné.
    List<Client> findByRole(Client.Role role);


     // Recherche des clients par nom
    @Query("SELECT c FROM Client c WHERE LOWER(c.nomComplet) LIKE LOWER(CONCAT('%', :nom, '%')) AND c.actif = true")
    Page<Client> rechercherParNom(@Param("nom") String nom, Pageable pageable);

    // Recherche multi-critères : nom OU email OU téléphone
    @Query("""
           SELECT c FROM Client c
           WHERE (LOWER(c.nomComplet) LIKE LOWER(CONCAT('%', :terme, '%'))
               OR LOWER(c.email) LIKE LOWER(CONCAT('%', :terme, '%'))
               OR c.telephone LIKE CONCAT('%', :terme, '%'))
           AND c.actif = true
           """)
    Page<Client> rechercherParTerme(@Param("terme") String terme, Pageable pageable);


     // Compte les clients actifs.
    long countByActifTrue();

    //Compte les clients par ville.
    long countByVille(String ville);

    //Compte les nouveaux inscrits depuis une date donnée.

    long countByDateInscriptionAfter(LocalDateTime date);

   //Met à jour la date de dernière connexion directement en BDD.
    @Modifying
    @Query("UPDATE Client c SET c.derniereConnexion = :date WHERE c.id = :id")
    void mettreAJourDerniereConnexion(@Param("id") Long id, @Param("date") LocalDateTime date);

    //Désactive un client
    @Modifying
    @Query("UPDATE Client c SET c.actif = false WHERE c.id = :id")
    void desactiverClient(@Param("id") Long id);
}
