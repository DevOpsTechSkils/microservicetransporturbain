package cm.transport.service_clients_2.config;



import cm.transport.service_clients_2.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
// prePostEnabled = true , active @PreAuthorize("hasRole('ROLE_ADMIN')")
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // Notre filtre JWT (injecté par Spring)
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Notre service qui charge les utilisateurs depuis la BDD
    private final UserDetailsService userDetailsService;

    //SecurityFilterChain = la configuration principale des règles de sécurité.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF (Cross-Site Request Forgery) = protection contre les fausses requêtes
                // On DÉSACTIVE CSRF car on utilise JWT (stateless)
                // JWT protège déjà contre ce type d'attaque
                .csrf(AbstractHttpConfigurer::disable)

                // CORS = Cross-Origin Resource Sharing
                // Permet au frontend  d'appeler notre API (localhost:8081)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //AUTORISATIONS DES ENDPOINTS
                .authorizeHttpRequests(auth -> auth

                                // Inscription et login : tout le monde peut accéder
                                .requestMatchers("/api/auth/**").permitAll()

                                // Swagger UI : accessible sans authentification
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs"
                                ).permitAll()

                                // Console H2 : accès libre en développement
                                .requestMatchers("/h2-console/**").permitAll()

                                // Actuator health : accessible pour les health checks
                                .requestMatchers("/actuator/health").permitAll()

                                // Seuls les utilisateurs avec ROLE_ADMIN peuvent accéder
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                // hasRole("ADMIN") cherche automatiquement "ROLE_ADMIN"

                                // TOUS LES AUTRES ENDPOINTS : nécessitent une authentification
                                .anyRequest().authenticated()
                        // authenticated() = l'utilisateur doit avoir un token JWT valide
                )

                //  GESTION DES SESSIONS
                // STATELESS = Spring Security ne crée PAS de session HTTP (HttpSession)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  PROVIDER D'AUTHENTIFICATION
                // Indique à Spring Security comment vérifier les identifiants
                .authenticationProvider(authenticationProvider())

                // AJOUT DU FILTRE JWT
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                //  HEADERS
                // Permet à la console H2 de s'afficher dans une iframe
                // (H2 console utilise des frames, bloquées par défaut)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    // CONFIGURATION CORS

    //Configuration CORS — Autorise les requêtes cross-origin du frontend React.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (en développement)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:42000",    // Angular dev server
                "http://localhost:3001",    // Port alternatif
                "http://127.0.0.1:3000",
                "http://localhost:8080"     // API Gateway
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers autorisés dans les requêtes
        configuration.setAllowedHeaders(List.of("*")); // Tout autoriser

        // Autoriser l'envoi de cookies et du header Authorization
        configuration.setAllowCredentials(true);

        // Durée de mise en cache de la réponse preflight (en secondes)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer cette config à tous les chemins
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // BEANS D'AUTHENTIFICATION

    //AuthenticationProvider = composant qui authentifie les utilisateurs.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        // Indique COMMENT charger l'utilisateur (depuis notre BDD)
        //authProvider.setUserDetailsService(userDetailsService);
        // Indique COMMENT vérifier le mot de passe (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

   //AuthenticationManager = orchestre le processus d'authentification.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

   // PasswordEncoder = encodeur de mots de passe.
    // BCrypt = algorithme de hachage sécurisé pour les mots de passe.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
