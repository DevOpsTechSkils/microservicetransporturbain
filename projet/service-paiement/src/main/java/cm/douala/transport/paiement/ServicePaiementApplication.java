package cm.douala.transport.paiement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Point d'entrée du Service Paiement.
 * Gère les paiements (Mobile Money, etc.).
 * À compléter par Dev 4.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServicePaiementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicePaiementApplication.class, args);
    }
}
