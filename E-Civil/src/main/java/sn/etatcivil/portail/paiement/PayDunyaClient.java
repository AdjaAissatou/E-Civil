package sn.etatcivil.portail.paiement;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * API « Paiement Avec Redirection » de PayDunya : création de facture puis confirmation.
 * Documentation : https://developers.paydunya.com/doc/FR/http_json
 */
@Component
public class PayDunyaClient {

    private final ProprietesPortail.Paydunya config;
    private final RestClient http;

    public PayDunyaClient(RestClient.Builder builder, ProprietesPortail proprietes) {
        this.config = proprietes.paydunya();
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(10));
        fabrique.setReadTimeout(Duration.ofSeconds(30));
        this.http = builder
                .baseUrl(config.urlApi())
                .requestFactory(fabrique)
                .defaultHeader("PAYDUNYA-MASTER-KEY", valeur(config.masterKey()))
                .defaultHeader("PAYDUNYA-PRIVATE-KEY", valeur(config.privateKey()))
                .defaultHeader("PAYDUNYA-TOKEN", valeur(config.token()))
                .build();
    }

    public record Facture(String token, String urlPaiement) {}

    public record EtatFacture(String statut, BigDecimal montant, String urlRecu, String referenceDemande) {
        public boolean estPayee() { return "completed".equals(statut); }
        public boolean estAnnulee() { return "cancelled".equals(statut); }
    }

    public Facture creerFacture(int montant, String description, String referenceDemande,
                                String urlRetour, String urlAnnulation, String urlNotification) {
        exigerConfiguration();
        Map<String, Object> corps = Map.of(
                "invoice", Map.of("total_amount", montant, "description", description),
                "store", Map.of("name", config.nomBoutique()),
                "custom_data", Map.of("reference_demande", referenceDemande),
                "actions", Map.of("return_url", urlRetour, "cancel_url", urlAnnulation,
                        "callback_url", urlNotification));
        JsonNode r = appeler(() -> http.post().uri("/checkout-invoice/create")
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(JsonNode.class));
        if (!"00".equals(r.path("response_code").asText())) {
            throw new ActionRefuseeException("PayDunya a refusé la création du paiement : "
                    + r.path("response_text").asText("erreur inconnue"));
        }
        return new Facture(r.path("token").asText(), r.path("response_text").asText());
    }

    public EtatFacture confirmer(String token) {
        exigerConfiguration();
        JsonNode r = appeler(() -> http.get().uri("/checkout-invoice/confirm/{token}", token)
                .retrieve().body(JsonNode.class));
        if (!"00".equals(r.path("response_code").asText())) {
            return new EtatFacture("inconnu", BigDecimal.ZERO, null, null);
        }
        JsonNode montant = r.path("invoice").path("total_amount");
        return new EtatFacture(
                r.path("status").asText(),
                montant.isMissingNode() || montant.asText().isBlank() ? BigDecimal.ZERO : new BigDecimal(montant.asText()),
                r.hasNonNull("receipt_url") ? r.get("receipt_url").asText() : null,
                r.path("custom_data").path("reference_demande").asText(null));
    }

    public boolean estConfigure() {
        return config.configure();
    }

    public String masterKey() {
        return config.masterKey();
    }

    private void exigerConfiguration() {
        if (!config.configure()) {
            throw new ActionRefuseeException("Le paiement en ligne n'est pas encore configuré sur ce portail. "
                    + "Contactez l'administration.");
        }
    }

    private JsonNode appeler(java.util.function.Supplier<JsonNode> appel) {
        try {
            JsonNode r = appel.get();
            if (r == null) throw new ActionRefuseeException("Réponse vide de PayDunya");
            return r;
        } catch (RestClientException e) {
            throw new ActionRefuseeException("Le service de paiement PayDunya est momentanément indisponible, "
                    + "veuillez réessayer dans quelques minutes");
        }
    }

    private static String valeur(String s) {
        return s == null ? "" : s;
    }
}
