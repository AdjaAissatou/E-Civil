package sn.etatcivil.paiement.adaptateur.sortie.paydunya;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.etatcivil.paiement.application.port.sortie.AdressesRetour;
import sn.etatcivil.paiement.application.port.sortie.PasserellePaiement;

import java.util.UUID;

/** Le citoyen revient sur le portail ; PayDunya notifie l'API publique (via la passerelle). */
@Component
class AdressesPortail implements AdressesRetour {

    private final String base;

    AdressesPortail(@Value("${ecivil.url-publique:http://localhost:8080}") String base) {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    @Override
    public PasserellePaiement.Retours pour(UUID demandeId) {
        return new PasserellePaiement.Retours(base + "/paiements/retour", base + "/paiements/annulation?demande=" + demandeId,
                base + "/api/paiements/paydunya/ipn");
    }
}
