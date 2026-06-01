package cm.transport.service_clients_2.security;

import cm.transport.service_clients_2.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.debug("Chargement utilisateur : {}", email);

        return clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Utilisateur introuvable : {}", email);

                    return new UsernameNotFoundException(
                            "Utilisateur introuvable : " + email
                    );
                });
    }
}
