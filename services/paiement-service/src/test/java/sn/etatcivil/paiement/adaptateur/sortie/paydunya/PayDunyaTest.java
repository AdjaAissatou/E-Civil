package sn.etatcivil.paiement.adaptateur.sortie.paydunya;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import sn.etatcivil.paiement.application.port.sortie.PasserellePaiement;
import sn.etatcivil.paiement.domaine.EtatFacture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Dialogue HTTP avec l'API PayDunya, rejoué sur un serveur local. */
class PayDunyaTest {

    HttpServer serveur;
    final List<String> requetes = new ArrayList<>();

    @AfterEach
    void arreter() { if (serveur != null) serveur.stop(0); }

    @Test
    void creationConfirmationEtNotification() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/checkout-invoice/create", e -> repondre(e, """
                {"response_code":"00","response_text":"https://paydunya.com/sandbox-checkout/invoice/test_abc","token":"test_abc"}"""));
        serveur.createContext("/checkout-invoice/confirm/test_abc", e -> repondre(e, """
                {"response_code":"00","status":"completed","invoice":{"token":"test_abc","total_amount":"1000"},
                 "receipt_url":"https://paydunya.com/receipt/test_abc"}"""));
        serveur.start();
        var config = new PayDunya.Configuration("test", "MK", "PK", "TK", "E-Civil", "http://127.0.0.1:" + serveur.getAddress().getPort());
        PayDunya paydunya = new PayDunya(RestClient.builder(), config);

        var f = paydunya.creerFacture(1000, "Extrait", "DEM-2026-000001",
                new PasserellePaiement.Retours("http://p/retour", "http://p/annulation", "http://p/api/paiements/paydunya/ipn"));
        assertThat(f.token()).isEqualTo("test_abc");
        assertThat(requetes.get(0)).contains("master=MK private=PK token=TK", "\"total_amount\":1000",
                "\"callback_url\":\"http://p/api/paiements/paydunya/ipn\"", "\"reference_demande\":\"DEM-2026-000001\"");

        EtatFacture etat = paydunya.consulter("test_abc");
        assertThat(etat.etat()).isEqualTo(EtatFacture.Etat.PAYEE);
        assertThat(etat.montant()).isEqualTo(1000);

        assertThat(paydunya.notificationAuthentique(PayDunya.sha512("MK"))).isTrue();
        assertThat(paydunya.notificationAuthentique(PayDunya.sha512("autre"))).isFalse();
    }

    @Test
    void urlParDefautSelonLeMode() {
        assertThat(new PayDunya.Configuration("test", null, null, null, null, null).urlApi()).isEqualTo("https://app.paydunya.com/sandbox-api/v1");
        assertThat(new PayDunya.Configuration("live", null, null, null, null, "").urlApi()).isEqualTo("https://app.paydunya.com/api/v1");
        assertThat(new PayDunya.Configuration("test", "MK", "", "TK", null, null).complete()).isFalse();
    }

    private void repondre(HttpExchange e, String json) throws IOException {
        var h = e.getRequestHeaders();
        requetes.add(e.getRequestURI().getPath() + " master=" + h.getFirst("PAYDUNYA-MASTER-KEY") + " private="
                + h.getFirst("PAYDUNYA-PRIVATE-KEY") + " token=" + h.getFirst("PAYDUNYA-TOKEN") + " "
                + new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] corps = json.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().add("Content-Type", "application/json");
        e.sendResponseHeaders(200, corps.length);
        e.getResponseBody().write(corps);
        e.close();
    }
}
