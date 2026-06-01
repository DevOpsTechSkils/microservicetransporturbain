package cm.transport.service_clients_2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    // @Value = injecte la valeur depuis application.yml
    @Value("${application.security.jwt.secret-key}")
    private String cleSecrete;  // La clé secrète pour signer les tokens

    @Value("${application.security.jwt.expiration}")
    private long dureeExpiration;  // Durée en millisecondes (86400000 = 24h)

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long dureeRefreshToken;  // Durée du refresh token (604800000 = 7j)

   // Génère un token JWT pour un utilisateur.
    public String genererToken(UserDetails userDetails) {
        Map<String, Object> claimsSupplementaires = new HashMap<>();

        // Récupère le premier rôle de l'utilisateur et l'ajoute dans le payload JWT
        userDetails.getAuthorities().stream()
                .findFirst()
                .ifPresent(auth -> claimsSupplementaires.put("role", auth.getAuthority()));

        return genererToken(claimsSupplementaires, userDetails);
    }

    //Génère un token JWT avec des claims personnalisés.
    public String genererToken(Map<String, Object> claimsSupplementaires, UserDetails userDetails) {
        return construireToken(claimsSupplementaires, userDetails, dureeExpiration);
    }

    public String genererRefreshToken(UserDetails userDetails) {
        // Le refresh token n'a pas de claims supplémentaires
        return construireToken(new HashMap<>(), userDetails, dureeRefreshToken);
    }

    private String construireToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long duree) {

        long maintenant = System.currentTimeMillis();

        return Jwts.builder()
                // claims() = ajoute les données personnalisées dans le payload
                .claims(claims)
                // subject = l'identifiant principal (ici l'email)
                .subject(userDetails.getUsername())
                // issuedAt = date de création du token
                .issuedAt(new Date(maintenant))
                // expiration = date d'expiration (maintenant + durée)
                .expiration(new Date(maintenant + duree))
                // signWith = signe le token avec notre clé secrète
                // La signature garantit que le token n'a pas été modifié
                .signWith(getCleSignature())
                // compact() = construit et encode le token final (String Base64)
                .compact();
    }

    //Vérifie qu'un token est valide pour un utilisateur donné.
    public boolean estTokenValide(String token, UserDetails userDetails) {
        try {
            // Extrait l'email (username) du token
            final String email = extraireEmail(token);
            return email.equals(userDetails.getUsername())
                    && !estTokenExpire(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expiré : {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformé : {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Signature JWT invalide : {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erreur de validation JWT : {}", e.getMessage());
            return false;
        }
    }


    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    //Extrait la date d'expiration du token.
    public Date extraireExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    public <T> T extraireClaim(String token, Function<Claims, T> resolverClaim) {
        // Extrait tous les claims (payload) du token
        final Claims claims = extraireTousClaims(token);
        // Applique la fonction pour récupérer le claim voulu
        return resolverClaim.apply(claims);
    }

    private Claims extraireTousClaims(String token) {
        return Jwts.parser()
                .verifyWith(getCleSignature())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean estTokenExpire(String token) {
        // Comparaison de la date d'expiration avec la date actuelle
        // before(new Date()) = la date d'expiration est AVANT maintenant → expiré
        return extraireExpiration(token).before(new Date());
    }

    private SecretKey getCleSignature() {
        // Converts the hex string to bytes for the key
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(cleSecrete);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getDureeExpirationEnSecondes() {
        return dureeExpiration / 1000;
    }
}
