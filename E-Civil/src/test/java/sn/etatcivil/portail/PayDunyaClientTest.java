package sn.etatcivil.portail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.paiement.PayDunyaClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Vérifie le dialogue HTTP avec PayDunya (en-têtes, corps, lecture des réponses) sur un serveur local. */
class PayDunyaClientTest {

    HttpServer serveur;
    final List<String> requetes = new ArrayList<>();

    @AfterEach
    void arreter() {
        if (serveur != null) serveur.stop(0);
    }

    @Test
    void creeEtConfirmeUneFactureSelonLApiPayDunya() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/checkout-invoice/create", e -> repondre(e, """
                {"response_code":"00","response_text":"https://paydunya.com/sandbox-checkout/invoice/test_abc",
                 "description":"Checkout Invoice Created","token":"test_abc"}"""));
        serveur.createContext("/checkout-invoice/confirm/test_abc", e -> repondre(e, """
                {"response_code":"00","response_text":"Transaction Found","status":"completed",
                 "invoice":{"token":"test_abc","total_amount":"1000"},
                 "custom_data":{"reference_demande":"DEM-2026-000001"},
                 "receipt_url":"https://paydunya.com/receipt/test_abc"}"""));
        serveur.start();
        String url = "http://127.0.0.1:" + serveur.getAddress().getPort();
        var proprietes = new ProprietesPortail("http://localhost:8080", null, null,
                new ProprietesPortail.Paydunya("test", "MK", "PK", "TK", "E-Civil", url), null);
        PayDunyaClient client = new PayDunyaClient(RestClient.builder(), proprietes);

        var facture = client.creerFacture(1000, "Extrait", "DEM-2026-000001", "http://localhost:8080/paiements/retour",
                "http://localhost:8080/paiements/annulation?demande=1", "http://localhost:8080/paiements/paydunya/ipn");
        assertThat(facture.token()).isEqualTo("test_abc");
        assertThat(facture.urlPaiement()).startsWith("https://paydunya.com/");

        String creation = requetes.get(0);
        assertThat(creation).contains("POST /checkout-invoice/create", "master=MK", "private=PK", "token=TK",
                "\"total_amount\":1000", "\"callback_url\":\"http://localhost:8080/paiements/paydunya/ipn\"",
                "\"reference_demande\":\"DEM-2026-000001\"");

        var etat = client.confirmer("test_abc");
        assertThat(etat.estPayee()).isTrue();
        assertThat(etat.montant()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(etat.urlRecu()).isEqualTo("https://paydunya.com/receipt/test_abc");
        assertThat(etat.referenceDemande()).isEqualTo("DEM-2026-000001");
    }

    @Test
    void urlParDefautSelonLeMode() {
        assertThat(new ProprietesPortail.Paydunya("test", null, null, null, null, null).urlApi())
                .isEqualTo("https://app.paydunya.com/sandbox-api/v1");
        assertThat(new ProprietesPortail.Paydunya("live", null, null, null, null, "").urlApi())
                .isEqualTo("https://app.paydunya.com/api/v1");
    }

    private void repondre(HttpExchange e, String json) throws IOException {
        var h = e.getRequestHeaders();
        requetes.add(e.getRequestMethod() + " " + e.getRequestURI().getPath()
                + " master=" + h.getFirst("PAYDUNYA-MASTER-KEY") + " private=" + h.getFirst("PAYDUNYA-PRIVATE-KEY")
                + " token=" + h.getFirst("PAYDUNYA-TOKEN") + " "
                + new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] corps = json.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().add("Content-Type", "application/json");
        e.sendResponseHeaders(200, corps.length);
        e.getResponseBody().write(corps);
        e.close();
    }
}
