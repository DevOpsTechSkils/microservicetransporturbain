package cm.transport.service_clients_2.service;



import cm.transport.service_clients_2.dto.AuthReponseDTO;
import cm.transport.service_clients_2.dto.InscriptionDTO;
import cm.transport.service_clients_2.dto.LoginDTO;
import cm.transport.service_clients_2.dto.ProfilDTO;
import cm.transport.service_clients_2.exception.ClientDejaExistantException;
import cm.transport.service_clients_2.exception.MotDePasseInvalideException;
import cm.transport.service_clients_2.model.Client;
import cm.transport.service_clients_2.repository.ClientRepository;
import cm.transport.service_clients_2.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService /*implements UserDetailsService*/ {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;          // BCryptPasswordEncoder
    private final JwtService jwtService;                    // Notre service JWT
    private final AuthenticationManager authenticationManager; // Gestionnaire auth Spring

    // Inscrit un nouveau client voyageur.
    @Transactional
    public AuthReponseDTO inscrire(InscriptionDTO dto) {
        log.info("Tentative d'inscription pour l'email : {}", dto.getEmail());

        if (!dto.getMotDePasse().equals(dto.getConfirmationMotDePasse())) {
            throw new MotDePasseInvalideException(
                    "Le mot de passe et sa confirmation ne correspondent pas"
            );
        }

        if (clientRepository.existsByEmail(dto.getEmail())) {
            log.warn("Tentative d'inscription avec email déjà existant : {}", dto.getEmail());
            throw new ClientDejaExistantException(
                    "Un compte existe déjà avec l'email : " + dto.getEmail()
            );
        }

        if (clientRepository.existsByTelephone(dto.getTelephone())) {
            throw new ClientDejaExistantException(
                    "Un compte existe déjà avec le téléphone : " + dto.getTelephone()
            );
        }

        Client client = Client.builder()
                .nomComplet(dto.getNomComplet())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(dto.getMotDePasse()))
                .telephone(dto.getTelephone())
                .ville(dto.getVille())
                .numeroCNI(dto.getNumeroCNI())
                .role(Client.Role.ROLE_CLIENT) // Role par défaut
                .actif(true)
                .emailVerifie(false) // Email non vérifié à l'inscription
                .build();

        Client clientSauvegarde = clientRepository.save(client);
        log.info("Nouveau client créé : ID={}, email={}", clientSauvegarde.getId(), clientSauvegarde.getEmail());

        // On génère immédiatement un token pour que le client soit connecté dès son inscription
        String jwtToken = jwtService.genererToken(clientSauvegarde);

        return AuthReponseDTO.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .expiresDans(jwtService.getDureeExpirationEnSecondes())
                .client(ProfilDTO.depuisClient(clientSauvegarde))
                .build();
    }


    @Transactional
    public AuthReponseDTO connecter(LoginDTO dto) {
        log.info("Tentative de connexion pour : {}", dto.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),      // "principal" = identifiant
                            dto.getMotDePasse()
                    )
            );
        } catch (BadCredentialsException e) {
            // Email introuvable OU mot de passe incorrect
            log.warn("Échec de connexion pour {} : mauvais identifiants", dto.getEmail());
            throw new BadCredentialsException(
                    "Email ou mot de passe incorrect"
            );
        } catch (DisabledException e) {
            log.warn("Tentative de connexion sur compte désactivé : {}", dto.getEmail());
            throw new DisabledException(
                    "Ce compte a été désactivé. Contactez le support."
            );
        }

        Client client = clientRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Erreur interne : client introuvable après authentification"
                ));

        clientRepository.mettreAJourDerniereConnexion(client.getId(), LocalDateTime.now());

        String jwtToken = jwtService.genererToken(client);

        log.info("Connexion réussie pour : {}", dto.getEmail());

        return AuthReponseDTO.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .expiresDans(jwtService.getDureeExpirationEnSecondes())
                .client(ProfilDTO.depuisClient(client))
                .build();
    }

   // Charge un utilisateur depuis la BDD par son email (username).
   /* @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur par email : {}", email);
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Aucun compte trouvé pour l'email : {}", email);
                    return new UsernameNotFoundException(
                            "Aucun compte trouvé pour l'email : " + email
                    );
                });
    }*/
}
