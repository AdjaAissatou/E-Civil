package sn.etatcivil.paiement.adaptateur.sortie.paydunya;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sn.etatcivil.paiement.application.port.sortie.OperateurIndisponibleException;
import sn.etatcivil.paiement.application.port.sortie.PasserellePaiement;
import sn.etatcivil.paiement.domaine.EtatFacture;
import sn.etatcivil.paiement.domaine.RegleMetierException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Adaptateur PayDunya, API « Paiement Avec Redirection » : création de facture, confirmation,
 * authentification des notifications IPN (SHA-512 de la clé principale).
 * Documentation : https://developers.paydunya.com/doc/FR/http_json
 */
@Component
class PayDunya implements PasserellePaiement {

    @ConfigurationProperties(prefix = "ecivil.paydunya")
    record Configuration(String mode, String masterKey, String privateKey, String token, String nomBoutique, String url) {
        boolean complete() { return rempli(masterKey) && rempli(privateKey) && rempli(token); }

        String urlApi() {
            if (rempli(url)) return url;
            return "live".equalsIgnoreCase(mode) ? "https://app.paydunya.com/api/v1" : "https://app.paydunya.com/sandbox-api/v1";
        }

        private static boolean rempli(String s) { return s != null && !s.isBlank(); }
    }

    private final Configuration config;
    private final RestClient http;

    PayDunya(RestClient.Builder builder, Configuration config) {
        this.config = config;
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(10));
        fabrique.setReadTimeout(Duration.ofSeconds(30));
        this.http = builder.baseUrl(config.urlApi()).requestFactory(fabrique)
                .defaultHeader("PAYDUNYA-MASTER-KEY", valeur(config.masterKey()))
                .defaultHeader("PAYDUNYA-PRIVATE-KEY", valeur(config.privateKey()))
                .defaultHeader("PAYDUNYA-TOKEN", valeur(config.token()))
                .build();
    }

    @Override
    public Facture creerFacture(int montant, String description, String reference, Retours retours) {
        Map<String, Object> corps = Map.of(
                "invoice", Map.of("total_amount", montant, "description", description),
                "store", Map.of("name", config.nomBoutique()),
                "custom_data", Map.of("reference_demande", reference),
                "actions", Map.of("return_url", retours.urlRetour(), "cancel_url", retours.urlAnnulation(),
                        "callback_url", retours.urlNotification()));
        JsonNode r = appeler(() -> http.post().uri("/checkout-invoice/create").contentType(MediaType.APPLICATION_JSON)
                .body(corps).retrieve().body(JsonNode.class));
        if (!"00".equals(r.path("response_code").asText())) {
            throw new RegleMetierException("PayDunya a refusé la création du paiement : " + r.path("response_text").asText("erreur inconnue"));
        }
        return new Facture(r.path("token").asText(), r.path("response_text").asText());
    }

    @Override
    public EtatFacture consulter(String token) {
        JsonNode r = appeler(() -> http.get().uri("/checkout-invoice/confirm/{t}", token).retrieve().body(JsonNode.class));
        if (!"00".equals(r.path("response_code").asText())) return new EtatFacture(EtatFacture.Etat.INCONNUE, 0, null);
        EtatFacture.Etat etat = switch (r.path("status").asText()) {
            case "completed" -> EtatFacture.Etat.PAYEE;
            case "cancelled" -> EtatFacture.Etat.ANNULEE;
            case "pending" -> EtatFacture.Etat.EN_ATTENTE;
            default -> EtatFacture.Etat.INCONNUE;
        };
        String montant = r.path("invoice").path("total_amount").asText("0");
        return new EtatFacture(etat, montant.isBlank() ? 0 : new BigDecimal(montant).longValueExact(),
                r.hasNonNull("receipt_url") ? r.get("receipt_url").asText() : null);
    }

    @Override
    public boolean notificationAuthentique(String signature) {
        if (!config.complete()) return false;
        byte[] attendu = sha512(config.masterKey()).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(attendu, signature.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean configuree() {
        return config.complete();
    }

    private static JsonNode appeler(Supplier<JsonNode> appel) {
        try {
            JsonNode r = appel.get();
            if (r == null) throw new OperateurIndisponibleException("Réponse vide de PayDunya", null);
            return r;
        } catch (RestClientException e) {
            throw new OperateurIndisponibleException("PayDunya est momentanément indisponible", e);
        }
    }

    static String sha512(String texte) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512").digest(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String valeur(String s) { return s == null ? "" : s; }
}
