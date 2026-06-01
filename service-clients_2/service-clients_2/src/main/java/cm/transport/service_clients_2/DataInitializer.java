package cm.transport.service_clients_2;


import cm.transport.service_clients_2.model.Client;
import cm.transport.service_clients_2.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Insère des données de test AU DÉMARRAGE (uniquement en profil "dev").
 // CommandLineRunner.run() est appelé automatiquement après le démarrage.

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // Ne s'exécute PAS en production
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // N'insère les données que si la base est vide
        if (clientRepository.count() > 0) {
            log.info("Base déjà initialisée — skip DataInitializer");
            return;
        }

        log.info("Initialisation des données de test...");

        // Admin
        // passwordEncoder.encode() = hash le mot de passe avec BCrypt
        // En prod, les admins sont créés autrement (pas ici)
        Client admin = Client.builder()
                .nomComplet("Administrateur Système")
                .email("admin@transport-cameroun.cm")
                .motDePasse(passwordEncoder.encode("Admin@2024!"))
                .telephone("699000001")
                .ville("Yaoundé")
                .role(Client.Role.ROLE_ADMIN)
                .actif(true)
                .emailVerifie(true)
                .build();

        // Clients voyageurs
        Client paul = Client.builder()
                .nomComplet("Tchamdjou Paul")
                .email("paul@gmail.com")
                .motDePasse(passwordEncoder.encode("Paul@2024!"))
                .telephone("677123456")
                .ville("Douala")
                .numeroCNI("CM001234567")
                .role(Client.Role.ROLE_CLIENT)
                .actif(true)
                .emailVerifie(true)
                .build();

        Client marie = Client.builder()
                .nomComplet("Ngono Marie Claire")
                .email("marie@gmail.com")
                .motDePasse(passwordEncoder.encode("Marie@2024!"))
                .telephone("695987654")
                .ville("Yaoundé")
                .numeroCNI("CM007654321")
                .role(Client.Role.ROLE_CLIENT)
                .actif(true)
                .emailVerifie(false)
                .build();

        Client jean = Client.builder()
                .nomComplet("Fomekong Jean-Baptiste")
                .email("jb@gmail.com")
                .motDePasse(passwordEncoder.encode("Jb@2024!"))
                .telephone("651456789")
                .ville("Bafoussam")
                .numeroCNI("CM009876543")
                .role(Client.Role.ROLE_CLIENT)
                .actif(true)
                .emailVerifie(true)
                .build();

        clientRepository.save(admin);
        clientRepository.save(paul);
        clientRepository.save(marie);
        clientRepository.save(jean);

        log.info(" {} comptes de test créés !", clientRepository.count());
        log.info("");
        log.info(" Comptes disponibles pour les tests :");
        log.info("    Admin  : admin@transport-cameroun.cm / Admin@2024!");
        log.info("    Client : paul@gmail.com / Paul@2024!");
        log.info("    Client : marie@gmail.com / Marie@2024!");
        log.info("");
    }
}