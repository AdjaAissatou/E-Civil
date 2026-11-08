package sn.etatcivil.central.securite;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Authentifie le portail E-Civil grâce à l'en-tête X-API-Key (comparaison à temps constant). */
public class FiltreCleApi extends OncePerRequestFilter {

    public static final String EN_TETE = "X-API-Key";

    private final byte[] cleAttendue;

    public FiltreCleApi(String cleAttendue) {
        this.cleAttendue = cleAttendue.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain chaine)
            throws ServletException, IOException {
        String fournie = requete.getHeader(EN_TETE);
        if (fournie != null && MessageDigest.isEqual(cleAttendue, fournie.getBytes(StandardCharsets.UTF_8))) {
            var auth = new UsernamePasswordAuthenticationToken("portail-e-civil", null,
                    AuthorityUtils.createAuthorityList("ROLE_PORTAIL"));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chaine.doFilter(requete, reponse);
    }
}
