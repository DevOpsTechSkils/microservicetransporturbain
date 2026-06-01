package cm.transport.service_clients_2.service;


import cm.transport.service_clients_2.dto.AuthReponseDTO;
import cm.transport.service_clients_2.dto.InscriptionDTO;
import cm.transport.service_clients_2.dto.LoginDTO;
import cm.transport.service_clients_2.exception.ClientDejaExistantException;
import cm.transport.service_clients_2.exception.MotDePasseInvalideException;
import cm.transport.service_clients_2.model.Client;
import cm.transport.service_clients_2.repository.ClientRepository;
import cm.transport.service_clients_2.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires d'AuthService.
 *
 * @ExtendWith(MockitoExtension.class) = Active Mockito pour JUnit 5
 * @Mock = Crée un faux objet contrôlé (pas d'appel réel BDD, pas de JWT réel...)
 * @InjectMocks = Crée une vraie instance et y injecte les mocks
 *
 * Principe AAA :
 *   Arrange → préparer les données et configurer les mocks
 *   Act     → appeler la méthode testée
 *   Assert  → vérifier le résultat
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires — AuthService")
public class AuthServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Données réutilisables dans les tests
    private InscriptionDTO dtoInscription;
    private Client clientSauvegarde;

    @BeforeEach
    void setUp() {
        // Prépare un DTO d'inscription valide
        dtoInscription = InscriptionDTO.builder()
                .nomComplet("Tchamdjou Paul")
                .email("paul@gmail.com")
                .motDePasse("Paul@2024!")
                .confirmationMotDePasse("Paul@2024!")
                .telephone("677123456")
                .ville("Douala")
                .numeroCNI("CM001234567")
                .build();

        // Simule le Client retourné après sauvegarde en BDD
        clientSauvegarde = Client.builder()
                .id(1L)
                .nomComplet("Tchamdjou Paul")
                .email("paul@gmail.com")
                .motDePasse("$2a$10$hashBcrypt")
                .telephone("677123456")
                .ville("Douala")
                .role(Client.Role.ROLE_CLIENT)
                .actif(true)
                .build();
    }

    // ─── Tests INSCRIPTION ────────────────────────────────────────

    @Nested
    @DisplayName("Inscription")
    class InscriptionTests {

        @Test
        @DisplayName(" Inscription réussie avec données valides")
        void inscription_donneesValides_retourneToken() {
            // ARRANGE — Configure les mocks pour simuler le comportement de la BDD
            when(clientRepository.existsByEmail("paul@gmail.com")).thenReturn(false);
            when(clientRepository.existsByTelephone("677123456")).thenReturn(false);
            when(passwordEncoder.encode("Paul@2024!")).thenReturn("$2a$10$hashBcrypt");
            when(clientRepository.save(any(Client.class))).thenReturn(clientSauvegarde);
            when(jwtService.genererToken(any())).thenReturn("jwt.token.test");
            when(jwtService.getDureeExpirationEnSecondes()).thenReturn(86400L);

            // ACT — Appel de la méthode testée
            AuthReponseDTO reponse = authService.inscrire(dtoInscription);

            // ASSERT — Vérifications
            assertThat(reponse).isNotNull();
            assertThat(reponse.getAccessToken()).isEqualTo("jwt.token.test");
            assertThat(reponse.getTokenType()).isEqualTo("Bearer");
            assertThat(reponse.getExpiresDans()).isEqualTo(86400L);
            assertThat(reponse.getClient()).isNotNull();
            assertThat(reponse.getClient().getEmail()).isEqualTo("paul@gmail.com");

            // Vérifie que save() a bien été appelé une fois
            verify(clientRepository, times(1)).save(any(Client.class));
            // Vérifie que le mot de passe a bien été hashé
            verify(passwordEncoder, times(1)).encode("Paul@2024!");
        }

        @Test
        @DisplayName(" Email déjà existant → ClientDejaExistantException")
        void inscription_emailDuplique_leveException() {
            // ARRANGE — L'email est déjà en base
            when(clientRepository.existsByEmail("paul@gmail.com")).thenReturn(true);

            // ACT + ASSERT — On vérifie que la bonne exception est levée
            assertThatThrownBy(() -> authService.inscrire(dtoInscription))
                    .isInstanceOf(ClientDejaExistantException.class)
                    .hasMessageContaining("paul@gmail.com");

            // IMPORTANT : save() ne doit PAS avoir été appelé
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName(" Téléphone déjà existant → ClientDejaExistantException")
        void inscription_telephoneDuplique_leveException() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByTelephone("677123456")).thenReturn(true);

            assertThatThrownBy(() -> authService.inscrire(dtoInscription))
                    .isInstanceOf(ClientDejaExistantException.class)
                    .hasMessageContaining("677123456");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName(" Mots de passe différents → MotDePasseInvalideException")
        void inscription_motDePasseNonConfirme_leveException() {
            // Modification du DTO : confirmation ne correspond pas
            dtoInscription.setConfirmationMotDePasse("AutreMdp@2024!");

            assertThatThrownBy(() -> authService.inscrire(dtoInscription))
                    .isInstanceOf(MotDePasseInvalideException.class)
                    .hasMessageContaining("correspondent pas");

            // Ni l'email ni le save ne doivent être vérifiés
            verify(clientRepository, never()).existsByEmail(any());
            verify(clientRepository, never()).save(any());
        }
    }

    // Tests LOGIN

    @Nested
    @DisplayName("Connexion")
    class LoginTests {

        @Test
        @DisplayName(" Login réussi avec bons identifiants")
        void login_identifiantsValides_retourneToken() {
            // ARRANGE
            LoginDTO dto = LoginDTO.builder()
                    .email("paul@gmail.com")
                    .motDePasse("Paul@2024!")
                    .build();

            // authenticationManager.authenticate() ne fait rien (mock → pas d'exception)
            // C'est le comportement attendu quand les identifiants sont corrects
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mock(org.springframework.security.core.Authentication.class));

            when(clientRepository.findByEmail("paul@gmail.com"))
                    .thenReturn(Optional.of(clientSauvegarde));
            when(jwtService.genererToken(any())).thenReturn("jwt.token.valid");
            when(jwtService.getDureeExpirationEnSecondes()).thenReturn(86400L);

            // ACT
            AuthReponseDTO reponse = authService.connecter(dto);

            // ASSERT
            assertThat(reponse.getAccessToken()).isEqualTo("jwt.token.valid");
            assertThat(reponse.getClient().getEmail()).isEqualTo("paul@gmail.com");

            // La date de dernière connexion doit être mise à jour
            verify(clientRepository, times(1))
                    .mettreAJourDerniereConnexion(eq(1L), any());
        }

        @Test
        @DisplayName(" Mauvais mot de passe -> BadCredentialsException")
        void login_mauvaisMotDePasse_leveException() {
            LoginDTO dto = LoginDTO.builder()
                    .email("paul@gmail.com")
                    .motDePasse("MauvaisMdp!")
                    .build();

            // authenticationManager lance BadCredentialsException quand les identifiants sont faux
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.connecter(dto))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("incorrect");

            // Le JWT ne doit PAS avoir été généré
            verify(jwtService, never()).genererToken(any());
        }

        @Test
        @DisplayName(" loadUserByUsername retourne le client pour un email valide")
        void loadUserByUsername_emailExistant_retourneClient() {
            when(clientRepository.findByEmail("paul@gmail.com"))
                    .thenReturn(Optional.of(clientSauvegarde));

            var userDetails = authService.loadUserByUsername("paul@gmail.com");

            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo("paul@gmail.com");
        }

        @Test
        @DisplayName(" loadUserByUsername email inexistant -> UsernameNotFoundException")
        void loadUserByUsername_emailInexistant_leveException() {
            when(clientRepository.findByEmail("inconnu@gmail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.loadUserByUsername("inconnu@gmail.com"))
                    .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
        }
    }
}
