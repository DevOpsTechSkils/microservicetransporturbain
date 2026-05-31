package cm.douala.transport.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Point d'entrée du Service Réservation.
 * Gère les réservations de trajets urbains.
 * À compléter par Dev 3.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceReservationApplication.class, args);
    }
}
