package cm.douala.transport.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Point d'entrée du Service Client.
 * Gère l'inscription, la connexion et le profil des utilisateurs.
 * À compléter par Dev 2.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceClientApplication.class, args);
    }
}
