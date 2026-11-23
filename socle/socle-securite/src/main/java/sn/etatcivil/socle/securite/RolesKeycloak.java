package sn.etatcivil.socle.securite;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Transforme les rôles du royaume Keycloak (claim realm_access.roles) en autorités Spring « ROLE_… ». */
public class RolesKeycloak implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, autorites(jwt), jwt.getSubject());
    }

    public static Collection<GrantedAuthority> autorites(Map<String, Object> claims) {
        return roles(claims).stream().<GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    }

    @SuppressWarnings("unchecked")
    public static List<String> roles(Map<String, Object> claims) {
        Object acces = claims.get("realm_access");
        if (acces instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    private static Collection<GrantedAuthority> autorites(Jwt jwt) {
        return autorites(jwt.getClaims());
    }
}
