package sn.etatcivil.portail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Toute la configuration externe du portail ; les secrets viennent des variables d'environnement. */
@ConfigurationProperties(prefix = "ecivil")
public record ProprietesPortail(String urlPublique, Admin admin, Registre registre, Paydunya paydunya, Mail mail) {

    public record Admin(String email, String motDePasse) {}

    public record Registre(String url, String apiKey) {}

    /** {@code url} est facultative : par défaut elle est déduite du mode (sandbox ou production). */
    public record Paydunya(String mode, String masterKey, String privateKey, String token, String nomBoutique, String url) {
        public boolean configure() {
            return estRempli(masterKey) && estRempli(privateKey) && estRempli(token);
        }

        public String urlApi() {
            if (estRempli(url)) return url;
            return "live".equalsIgnoreCase(mode)
                    ? "https://app.paydunya.com/api/v1"
                    : "https://app.paydunya.com/sandbox-api/v1";
        }
    }

    public record Mail(String expediteur) {}

    public String lien(String chemin) {
        String base = urlPublique.endsWith("/") ? urlPublique.substring(0, urlPublique.length() - 1) : urlPublique;
        return base + chemin;
    }

    static boolean estRempli(String s) {
        return s != null && !s.isBlank();
    }
}
