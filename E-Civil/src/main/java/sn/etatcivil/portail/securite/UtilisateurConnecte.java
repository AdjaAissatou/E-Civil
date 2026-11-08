package sn.etatcivil.portail.securite;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sn.etatcivil.portail.utilisateur.Role;
import sn.etatcivil.portail.utilisateur.Utilisateur;

import java.util.Collection;
import java.util.List;

/** Identité stockée en session : seulement des valeurs simples, jamais l'entité JPA. */
public class UtilisateurConnecte implements UserDetails {

    private final Long id;
    private final String email;
    private final String motDePasse;
    private final String nomComplet;
    private final Role role;
    private final boolean actif;

    public UtilisateurConnecte(Utilisateur u) {
        this.id = u.getId();
        this.email = u.getEmail();
        this.motDePasse = u.getMotDePasse();
        this.nomComplet = u.getNomComplet();
        this.role = u.getRole();
        this.actif = u.isActif();
    }

    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public Role getRole() { return role; }

    /** Libellé enregistré dans l'historique des demandes. */
    public String signature() { return nomComplet + " (" + role.getLibelle() + ")"; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return motDePasse; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return actif; }
}
