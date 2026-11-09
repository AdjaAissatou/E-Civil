package sn.etatcivil.notification.adaptateur.sortie.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.etatcivil.notification.application.port.sortie.LiensPortail;

@Component
class LiensPortailWeb implements LiensPortail {

    private final String urlPublique;

    LiensPortailWeb(@Value("${ecivil.url-publique:http://localhost:8080}") String urlPublique) {
        this.urlPublique = urlPublique.endsWith("/") ? urlPublique.substring(0, urlPublique.length() - 1) : urlPublique;
    }

    @Override
    public String demande(String demandeId) {
        return urlPublique + "/demandes/" + demandeId;
    }
}
