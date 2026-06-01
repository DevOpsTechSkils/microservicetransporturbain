package cm.transport.service_clients_2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = classe de base Spring garantissant l'exécution

    //  service JWT pour extraire et valider les tokens
    private final JwtService jwtService;

    // UserDetailsService = interface Spring Security pour charger un utilisateur
    private final UserDetailsService userDetailsService;

    // doFilterInternal() = méthode principale du filtre, Appelée automatiquement pour CHAQUE requête HTTP.
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,      // Requête HTTP (contient les headers)
            @NonNull HttpServletResponse response,    // Réponse HTTP
            @NonNull FilterChain filterChain          // Chaîne de filtres suivants
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        log.debug("Requête : {} {} | Authorization présente : {}",
                request.getMethod(), request.getRequestURI(), authHeader != null);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // filterChain.doFilter() = passe la requete au filtre suivant dans la chaine
            filterChain.doFilter(request, response);
            return; // On arrête le traitement de CE filtre ici
        }

        final String jwtToken = authHeader.substring(7);

       // Extraction de l'email depuis le token
        String emailUtilisateur;
        try {
            emailUtilisateur = jwtService.extraireEmail(jwtToken);
        } catch (Exception e) {
            // Token malformé ou signature invalide , on passe sans authentifier
            log.warn("Impossible d'extraire l'email du token : {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        //  Vérification si l'utilisateur n'est pas déjà authentifié
        if (emailUtilisateur != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(emailUtilisateur);

            if (jwtService.estTokenValide(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Ajoute les détails de la requête HTTP dans l'authentification
                // (adresse IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Utilisateur authentifié via JWT : {} | Rôle : {}",
                        emailUtilisateur, userDetails.getAuthorities());
            } else {
                log.warn("Token JWT invalide pour l'utilisateur : {}", emailUtilisateur);
            }
        }

        filterChain.doFilter(request, response);
    }
}

