package cm.transport.service_clients_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  SERVICE CLIENTS — Point d'entrée principal                      ║
 * ║                                                                  ║
 * ║  @SpringBootApplication = 3 annotations en une :                 ║
 * ║    @Configuration      = cette classe peut définir des beans     ║
 * ║    @EnableAutoConfiguration = Spring configure automatiquement   ║
 * ║    @ComponentScan      = scanne tous les sous-packages           ║
 * ║                                                                  ║
 * ║  @EnableDiscoveryClient = s'enregistre dans Eureka au démarrage  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceClients2Application {

	public static void main(String[] args) {
		SpringApplication.run(ServiceClients2Application.class, args);

		System.out.println("""
                Service Clients démarré avec succès !              
                                                                      
                API REST   : http://localhost:8081                  
                Swagger UI : http://localhost:8081/swagger-ui.html  
               ️  H2 Console: http://localhost:8081/h2-console        
               ️  Health    : http://localhost:8081/actuator/health   
                                                                      
               Développeur : Dev2                                     
            """);
	}
}