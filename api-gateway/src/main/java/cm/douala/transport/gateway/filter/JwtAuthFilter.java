package cm.douala.transport.gateway.filter;

import cm.douala.transport.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtre JWT global — appliqué sur TOUTES les requêtes.
 * Analogy : le maître d'hôtel qui vérifie chaque ticket d'entrée
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Routes accessibles SANS token (publiques)
    private static final List<String> PUBLIC_ROUTES = List.of(
        "/api/clients/auth/register",
        "/api/clients/auth/login",
        "/actuator"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Laisser passer les routes publiques
        boolean isPublic = PUBLIC_ROUTES.stream()
            .anyMatch(path::startsWith);

        if (isPublic) {
            return chain.filter(exchange);
        }

        // Vérifier la présence du header Authorization
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extraire et valider le token
        String token = authHeader.substring(7); // Enlever "Bearer "

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Ajouter l'identité dans les headers pour les microservices en aval
        String username = jwtUtil.extractUsername(token);
        ServerHttpRequest mutatedRequest = exchange.getRequest()
            .mutate()
            .header("X-User-Email", username)
            .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // Exécuté en premier (priorité haute)
    }
}
