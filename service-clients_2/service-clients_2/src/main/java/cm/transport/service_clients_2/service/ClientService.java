package cm.transport.service_clients_2.service;


import cm.transport.service_clients_2.dto.ChangementMotDePasseDTO;
import cm.transport.service_clients_2.dto.MiseAJourProfilDTO;
import cm.transport.service_clients_2.dto.ProfilDTO;
import cm.transport.service_clients_2.exception.ClientDejaExistantException;
import cm.transport.service_clients_2.exception.ClientNotFoundException;
import cm.transport.service_clients_2.exception.MotDePasseInvalideException;
import cm.transport.service_clients_2.model.Client;
import cm.transport.service_clients_2.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    // Récupère le profil du CLIENT ACTUELLEMENT CONNECTÉ.
    @Transactional(readOnly = true)
    public ProfilDTO getMonProfil() {
        Client client = getClientConnecte();
        log.debug("Récupération du profil : {}", client.getEmail());
        return ProfilDTO.depuisClient(client);
    }

    // Met à jour le profil du client connecté.
    @Transactional
    public ProfilDTO mettreAJourMonProfil(MiseAJourProfilDTO dto) {
        Client client = getClientConnecte();
        log.info("Mise à jour du profil : {}", client.getEmail());
        if (!client.getTelephone().equals(dto.getTelephone())
                && clientRepository.existsByTelephone(dto.getTelephone())) {
            throw new ClientDejaExistantException(
                    "Ce numéro de téléphone est déjà utilisé par un autre compte"
            );
        }

        // Mise à jour des champs modifiables
        client.setNomComplet(dto.getNomComplet());
        client.setTelephone(dto.getTelephone());
        client.setVille(dto.getVille());
        client.setNumeroCNI(dto.getNumeroCNI());
        client.setPhotoUrl(dto.getPhotoUrl());

        Client clientMisAJour = clientRepository.save(client);
        log.info("Profil mis à jour avec succès : {}", client.getEmail());

        return ProfilDTO.depuisClient(clientMisAJour);
    }

    //Change le mot de passe du client connecté.
    @Transactional
    public void changerMotDePasse(ChangementMotDePasseDTO dto) {
        Client client = getClientConnecte();
        log.info("Changement de mot de passe pour : {}", client.getEmail());

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(dto.getMotDePasseActuel(), client.getMotDePasse())) {
            throw new MotDePasseInvalideException(
                    "Le mot de passe actuel est incorrect"
            );
        }

        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmationNouveauMotDePasse())) {
            throw new MotDePasseInvalideException(
                    "Le nouveau mot de passe et sa confirmation ne correspondent pas"
            );
        }

        client.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        clientRepository.save(client);
        log.info("Mot de passe changé avec succès pour : {}", client.getEmail());
    }

    // OPÉRATIONS ADMIN

    // Liste tous les clients actifs avec pagination, RÉSERVÉ AUX ADMINS (@PreAuthorize).
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<ProfilDTO> listerTousLesClients(Pageable pageable) {
        log.info("Listing de tous les clients (admin)");
        // findByActifTrue() récupère uniquement les clients actifs
        return clientRepository.findByActifTrue(pageable)
                .map(ProfilDTO::depuisClient);
    }

   //Recherche des clients par terme (nom, email ou téléphone)., RÉSERVÉ AUX ADMINS.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Page<ProfilDTO> rechercherClients(String terme, Pageable pageable) {
        log.info("Recherche clients par terme : '{}'", terme);
        return clientRepository.rechercherParTerme(terme, pageable)
                .map(ProfilDTO::depuisClient);
    }

    // Récupère le profil d'un client par son ID.
    @Transactional(readOnly = true)
    public ProfilDTO getClientParId(Long id) {
        log.debug("Récupération du client ID : {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(
                        "Aucun client trouvé avec l'ID : " + id
                ));
        return ProfilDTO.depuisClient(client);
    }

    // Désactive un client (soft delete).
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void desactiverClient(Long id) {
        log.warn("Désactivation du client ID : {} par l'admin", id);

        // Vérifie que le client existe
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException("Aucun client trouvé avec l'ID : " + id);
        }

        clientRepository.desactiverClient(id);
        log.info("Client {} désactivé avec succès", id);
    }

    //Statistiques globales sur les clients.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de clients actifs
        stats.put("totalClientsActifs", clientRepository.countByActifTrue());

        // Nombre par ville (top villes)
        String[] villes = {"Douala", "Yaoundé", "Bafoussam", "Bamenda", "Garoua"};
        Map<String, Long> parVille = new HashMap<>();
        for (String ville : villes) {
            parVille.put(ville, clientRepository.countByVille(ville));
        }
        stats.put("clientsParVille", parVille);

        // Nombre de clients avec le rôle ADMIN
        stats.put("nombreAdmins",
                clientRepository.findByRole(Client.Role.ROLE_ADMIN).size());

        return stats;
    }

    //Récupère le client ACTUELLEMENT CONNECTÉ depuis le SecurityContext
    private Client getClientConnecte() {
        // Récupère l'objet Authentication du contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Aucun utilisateur authentifié");
        }
        // C'est notre entité Client (qui implémente UserDetails)
        String email = authentication.getName(); // getName() = getUsername() = email

        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client connecté introuvable en base : " + email
                ));
    }
}
