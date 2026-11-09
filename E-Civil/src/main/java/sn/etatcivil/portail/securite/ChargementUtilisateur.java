package sn.etatcivil.portail.securite;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sn.etatcivil.portail.utilisateur.UtilisateurRepository;

@Service
class ChargementUtilisateur implements UserDetailsService {

    private final UtilisateurRepository repository;

    ChargementUtilisateur(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return repository.findByEmailIgnoreCase(email.trim())
                .map(UtilisateurConnecte::new)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));
    }
}
