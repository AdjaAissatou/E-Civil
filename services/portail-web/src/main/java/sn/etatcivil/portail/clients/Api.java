package sn.etatcivil.portail.clients;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import sn.etatcivil.portail.clients.Vues.*;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** Appels aux microservices (via la passerelle). Une méthode par opération utilisée par les pages. */
@Component
public class Api {

    private final RestClient http;

    public Api(RestClient api) {
        this.http = api;
    }

    // ---------------------------------------------------------------- catalogue (public)

    public List<TypeDocument> documents() {
        return get("/api/catalogue/documents", new ParameterizedTypeReference<>() {});
    }

    public List<TypeDocument> tousLesDocuments() {
        return get("/api/catalogue/documents/tous", new ParameterizedTypeReference<>() {});
    }

    public TypeDocument document(Long id) {
        return appeler(() -> http.get().uri("/api/catalogue/documents/{id}", id).retrieve().body(TypeDocument.class));
    }

    public List<Motif> motifs() {
        return get("/api/catalogue/motifs", new ParameterizedTypeReference<>() {});
    }

    public void modifierDocument(Long id, int tarif, int delaiJours, boolean actif) {
        envoyer(() -> http.put().uri("/api/catalogue/documents/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tarif", tarif, "delaiJours", delaiJours, "actif", actif)).retrieve().toBodilessEntity());
    }

    // ---------------------------------------------------------------- demandes

    public Demande deposer(Object depot) {
        return appeler(() -> http.post().uri("/api/demandes").contentType(MediaType.APPLICATION_JSON).body(depot)
                .retrieve().body(Demande.class));
    }

    public List<Demande> mesDemandes() {
        return get("/api/demandes/mes-demandes", new ParameterizedTypeReference<>() {});
    }

    public Demande demande(UUID id) {
        return appeler(() -> http.get().uri("/api/demandes/{id}", id).retrieve().body(Demande.class));
    }

    public Page<Demande> demandes(Collection<String> statuts, String reference, int page) {
        return appeler(() -> http.get().uri(u -> {
            if (statuts != null) statuts.forEach(s -> u.queryParam("statut", s));
            if (reference != null && !reference.isBlank()) u.queryParam("reference", reference.trim());
            return u.path("/api/demandes").queryParam("page", page).build();
        }).retrieve().body(new ParameterizedTypeReference<Page<Demande>>() {}));
    }

    public Map<String, Long> statistiquesDemandes() {
        return get("/api/demandes/statistiques", new ParameterizedTypeReference<>() {});
    }

    /** Action sur une demande : validation, annulation, recherche, signature… */
    public void action(UUID id, String action, Map<String, String> corps) {
        envoyer(() -> {
            var r = http.post().uri("/api/demandes/{id}/{action}", id, action);
            if (corps != null) r.contentType(MediaType.APPLICATION_JSON).body(corps);
            return r.retrieve().toBodilessEntity();
        });
    }

    public Demande actionAvecResultat(UUID id, String action, Map<String, String> corps) {
        return appeler(() -> {
            var r = http.post().uri("/api/demandes/{id}/{action}", id, action);
            if (corps != null) r.contentType(MediaType.APPLICATION_JSON).body(corps);
            return r.retrieve().body(Demande.class);
        });
    }

    // ---------------------------------------------------------------- paiements

    public boolean paiementDisponible() {
        try {
            Map<?, ?> r = appeler(() -> http.get().uri("/api/paiements/disponibilite").retrieve().body(Map.class));
            return Boolean.TRUE.equals(r.get("disponible"));
        } catch (ApiException e) {
            return false;
        }
    }

    public String initierPaiement(UUID demandeId) {
        Map<?, ?> r = appeler(() -> http.post().uri("/api/paiements/demandes/{id}", demandeId).retrieve().body(Map.class));
        return (String) r.get("urlPaiement");
    }

    public Map<?, ?> confirmerPaiement(String token) {
        return appeler(() -> http.post().uri(u -> u.path("/api/paiements/confirmation").queryParam("token", token).build())
                .retrieve().body(Map.class));
    }

    public List<Paiement> paiements(UUID demandeId) {
        try {
            return appeler(() -> http.get().uri("/api/paiements/demandes/{id}", demandeId).retrieve()
                    .body(new ParameterizedTypeReference<List<Paiement>>() {}));
        } catch (ApiException e) {
            return List.of(); // l'historique des paiements est secondaire sur la fiche
        }
    }

    // ---------------------------------------------------------------- registre

    public Page<Acte> actes(String type, String statut, String nom, int page) {
        return appeler(() -> http.get().uri(u -> filtres(u.path("/api/registre/actes"), type, statut, nom, page))
                .retrieve().body(new ParameterizedTypeReference<Page<Acte>>() {}));
    }

    public Acte acte(UUID id) {
        return appeler(() -> http.get().uri("/api/registre/actes/{id}", id).retrieve().body(Acte.class));
    }

    public Acte transcrire(Object saisie) {
        return appeler(() -> http.post().uri("/api/registre/actes").contentType(MediaType.APPLICATION_JSON).body(saisie)
                .retrieve().body(Acte.class));
    }

    public Acte corriger(UUID id, Object saisie) {
        return appeler(() -> http.put().uri("/api/registre/actes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .body(saisie).retrieve().body(Acte.class));
    }

    public Acte validerActe(UUID id) {
        return appeler(() -> http.post().uri("/api/registre/actes/{id}/validation", id).retrieve().body(Acte.class));
    }

    public StatistiquesRegistre statistiquesRegistre() {
        return appeler(() -> http.get().uri("/api/registre/actes/statistiques").retrieve().body(StatistiquesRegistre.class));
    }

    // ---------------------------------------------------------------- documents

    public byte[] pdf(UUID demandeId) {
        return appeler(() -> http.get().uri("/api/documents/demandes/{id}/pdf", demandeId).retrieve().body(byte[].class));
    }

    public Optional<Verification> verifier(String code) {
        try {
            return Optional.ofNullable(appeler(() -> http.get().uri("/api/verification/{c}", code).retrieve()
                    .body(Verification.class)));
        } catch (ApiException e) {
            if (e.statut() == 404) return Optional.empty();
            throw e;
        }
    }

    // ---------------------------------------------------------------- interne

    private static URI filtres(UriBuilder u, String type, String statut, String nom, int page) {
        if (type != null && !type.isBlank()) u.queryParam("type", type);
        if (statut != null && !statut.isBlank()) u.queryParam("statut", statut);
        if (nom != null && !nom.isBlank()) u.queryParam("nom", nom);
        return u.queryParam("page", page).build();
    }

    private <T> T get(String chemin, ParameterizedTypeReference<T> type) {
        return appeler(() -> http.get().uri(chemin).retrieve().body(type));
    }

    private void envoyer(Supplier<?> appel) {
        appeler(appel);
    }

    private static <T> T appeler(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (ResourceAccessException e) {
            throw new ApiException(0, "Un service de l'état civil ne répond pas pour le moment. Réessayez dans quelques minutes.");
        }
    }
}
