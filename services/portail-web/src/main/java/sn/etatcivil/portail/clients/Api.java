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

    /** Motifs proposés pour une variété (communs et propres), avec leurs pièces à fournir. */
    public List<Motif> motifs(Long typeDocumentId) {
        return appeler(() -> http.get().uri(u -> u.path("/api/catalogue/motifs").queryParam("document", typeDocumentId).build())
                .retrieve().body(new ParameterizedTypeReference<List<Motif>>() {}));
    }

    public List<Motif> tousLesMotifs() {
        return get("/api/catalogue/motifs/tous", new ParameterizedTypeReference<>() {});
    }

    public List<Nature> natures(boolean activesSeulement) {
        return appeler(() -> http.get().uri(u -> u.path("/api/catalogue/natures").queryParam("actives", activesSeulement).build())
                .retrieve().body(new ParameterizedTypeReference<List<Nature>>() {}));
    }

    public ReferentielsCatalogue referentielsCatalogue() {
        return appeler(() -> http.get().uri("/api/catalogue/referentiels").retrieve().body(ReferentielsCatalogue.class));
    }

    public TypeDocument creerDocument(Map<String, Object> corps) {
        return appeler(() -> http.post().uri("/api/catalogue/documents").contentType(MediaType.APPLICATION_JSON).body(corps)
                .retrieve().body(TypeDocument.class));
    }

    public TypeDocument modifierDocumentComplet(Long id, Map<String, Object> corps) {
        return appeler(() -> http.put().uri("/api/catalogue/documents/{id}/caracteristiques", id)
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(TypeDocument.class));
    }

    public Nature enregistrerNature(Long id, Map<String, Object> corps) {
        return appeler(() -> (id == null ? http.post().uri("/api/catalogue/natures") : http.put().uri("/api/catalogue/natures/{id}", id))
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(Nature.class));
    }

    public Motif enregistrerMotif(Long id, Map<String, Object> corps) {
        return appeler(() -> (id == null ? http.post().uri("/api/catalogue/motifs") : http.put().uri("/api/catalogue/motifs/{id}", id))
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(Motif.class));
    }

    // ---------------------------------------------------------------- diaspora

    public ReferentielDiaspora diaspora() {
        try {
            return appeler(() -> http.get().uri("/api/catalogue/diaspora").retrieve().body(ReferentielDiaspora.class));
        } catch (ApiException e) {
            return new ReferentielDiaspora(List.of(), List.of(), List.of());
        }
    }

    public List<Mission> missions(Long paysId) {
        try {
            return appeler(() -> http.get().uri(u -> {
                u.path("/api/catalogue/diaspora/missions");
                if (paysId != null) u.queryParam("pays", paysId);
                return u.build();
            }).retrieve().body(new ParameterizedTypeReference<List<Mission>>() {}));
        } catch (ApiException e) {
            return List.of();
        }
    }

    public List<Mission> toutesLesMissions() {
        return get("/api/catalogue/diaspora/missions/toutes", new ParameterizedTypeReference<>() {});
    }

    public Mission enregistrerMission(Long id, Map<String, Object> corps) {
        return appeler(() -> (id == null ? http.post().uri("/api/catalogue/diaspora/missions")
                : http.put().uri("/api/catalogue/diaspora/missions/{id}", id))
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(Mission.class));
    }

    public Pays ajouterPays(Map<String, Object> corps) {
        return appeler(() -> http.post().uri("/api/catalogue/diaspora/pays").contentType(MediaType.APPLICATION_JSON)
                .body(corps).retrieve().body(Pays.class));
    }

    // ---------------------------------------------------------------- mon compte

    public ProfilPersonnel monProfil() {
        return appeler(() -> http.get().uri("/api/habilitations/moi").retrieve().body(ProfilPersonnel.class));
    }

    public ProfilPersonnel modifierMonProfil(ProfilPersonnel p) {
        return appeler(() -> http.put().uri("/api/habilitations/moi").contentType(MediaType.APPLICATION_JSON).body(p)
                .retrieve().body(ProfilPersonnel.class));
    }

    /** Identité déclarée par le demandeur à son inscription ; vide si le service ne répond pas. */
    public Optional<ProfilPersonnel> demandeur(String id) {
        try {
            return Optional.ofNullable(appeler(() -> http.get().uri("/api/habilitations/demandeurs/{id}", id).retrieve()
                    .body(ProfilPersonnel.class)));
        } catch (ApiException e) {
            return Optional.empty();
        }
    }

    public void preferencesNotifications(boolean notifications) {
        envoyer(() -> http.put().uri("/api/demandes/preferences").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("notifications", notifications)).retrieve().toBodilessEntity());
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

    /** Photo transmise telle que le navigateur l'a envoyée ; demande-service la contrôle et la réencode. */
    public record Photo(String nom, byte[] contenu, String contentType) {}

    /** Dépôt avec la pièce d'identité et le selfie (multipart). */
    public Demande deposer(Object depot, String typePiece, Map<String, Photo> photos) {
        var corps = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        var entete = new org.springframework.http.HttpHeaders();
        entete.setContentType(MediaType.APPLICATION_JSON);
        corps.add("demande", new org.springframework.http.HttpEntity<>(depot, entete));
        if (typePiece != null) corps.add("typePiece", typePiece);
        photos.forEach((nom, p) -> {
            if (p == null || p.contenu().length == 0) return;
            var h = new org.springframework.http.HttpHeaders();
            h.setContentType(MediaType.parseMediaType(p.contentType() == null ? "application/octet-stream" : p.contentType()));
            h.setContentDisposition(org.springframework.http.ContentDisposition.formData().name(nom)
                    .filename(p.nom() == null || p.nom().isBlank() ? nom : p.nom()).build());
            corps.add(nom, new org.springframework.http.HttpEntity<>(p.contenu(), h));
        });
        return appeler(() -> http.post().uri("/api/demandes").contentType(MediaType.MULTIPART_FORM_DATA).body(corps)
                .retrieve().body(Demande.class));
    }

    public List<PieceIdentite> piecesIdentite(UUID demandeId) {
        try {
            return appeler(() -> http.get().uri("/api/demandes/{id}/pieces", demandeId).retrieve()
                    .body(new ParameterizedTypeReference<List<PieceIdentite>>() {}));
        } catch (ApiException e) {
            return List.of();
        }
    }

    public byte[] photoIdentite(UUID demandeId, UUID pieceId) {
        return appeler(() -> http.get().uri("/api/demandes/{id}/pieces/{p}", demandeId, pieceId).retrieve().body(byte[].class));
    }

    public List<Demande> mesDemandes() {
        return get("/api/demandes/mes-demandes", new ParameterizedTypeReference<>() {});
    }

    public Demande demande(UUID id) {
        return appeler(() -> http.get().uri("/api/demandes/{id}", id).retrieve().body(Demande.class));
    }

    public Page<Demande> demandes(Collection<String> statuts, String reference, int page) {
        return demandes(statuts, new FiltreDemandes(reference, null, null, null, null), page);
    }

    /** Filtres des files des agents ; chaque critère vide est ignoré. */
    public record FiltreDemandes(String reference, Long typeDocument, String format, java.time.LocalDate du, java.time.LocalDate au) {
        public static final FiltreDemandes AUCUN = new FiltreDemandes(null, null, null, null, null);

        void appliquer(UriBuilder u) {
            if (reference != null && !reference.isBlank()) u.queryParam("reference", reference.trim());
            if (typeDocument != null) u.queryParam("typeDocument", typeDocument);
            if (format != null && !format.isBlank()) u.queryParam("format", format);
            if (du != null) u.queryParam("du", du);
            if (au != null) u.queryParam("au", au);
        }
    }

    public Page<Demande> demandes(Collection<String> statuts, FiltreDemandes f, int page) {
        return appeler(() -> http.get().uri(u -> {
            if (statuts != null) statuts.forEach(s -> u.queryParam("statut", s));
            f.appliquer(u);
            return u.path("/api/demandes").queryParam("page", page).build();
        }).retrieve().body(new ParameterizedTypeReference<Page<Demande>>() {}));
    }

    public Map<String, Long> statistiquesDemandes() {
        return statistiquesDemandes(FiltreDemandes.AUCUN);
    }

    public Map<String, Long> statistiquesDemandes(FiltreDemandes f) {
        return appeler(() -> http.get().uri(u -> {
            f.appliquer(u);
            return u.path("/api/demandes/statistiques").build();
        }).retrieve().body(new ParameterizedTypeReference<Map<String, Long>>() {}));
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

    public Acte ajouterMention(UUID id, String type, java.time.LocalDate date, String reference, String details) {
        var corps = new HashMap<String, Object>();
        corps.put("type", type);
        corps.put("date", date == null ? null : date.toString());
        corps.put("reference", reference);
        corps.put("details", details);
        return appeler(() -> http.post().uri("/api/registre/actes/{id}/mentions", id).contentType(MediaType.APPLICATION_JSON)
                .body(corps).retrieve().body(Acte.class));
    }

    public List<OperationActe> journalActe(UUID id) {
        return appeler(() -> http.get().uri("/api/registre/actes/{id}/journal", id).retrieve()
                .body(new ParameterizedTypeReference<List<OperationActe>>() {}));
    }

    /** Communes du Sénégal ; liste vide si le registre ne répond pas (le champ reste alors en saisie libre). */
    public List<Commune> communes() {
        try {
            return get("/api/registre/communes", new ParameterizedTypeReference<>() {});
        } catch (ApiException e) {
            return List.of();
        }
    }

    public List<TypeMention> typesMentions() {
        return get("/api/registre/types-mentions", new ParameterizedTypeReference<>() {});
    }

    public StatistiquesRegistre statistiquesRegistre() {
        return appeler(() -> http.get().uri("/api/registre/actes/statistiques").retrieve().body(StatistiquesRegistre.class));
    }

    // ---------------------------------------------------------------- renseignement

    public List<CentreEtatCivil> centres(String recherche, String type) {
        return appeler(() -> http.get().uri(u -> {
            if (recherche != null && !recherche.isBlank()) u.queryParam("recherche", recherche.trim());
            if (type != null && !type.isBlank()) u.queryParam("type", type);
            return u.path("/api/registre/centres").build();
        }).retrieve().body(new ParameterizedTypeReference<List<CentreEtatCivil>>() {}));
    }

    public CentreEtatCivil centre(long id) {
        return appeler(() -> http.get().uri("/api/registre/centres/{id}", id).retrieve().body(CentreEtatCivil.class));
    }

    public CentreEtatCivil enregistrerCentre(Long id, Map<String, Object> corps) {
        return appeler(() -> (id == null ? http.post().uri("/api/registre/centres") : http.put().uri("/api/registre/centres/{id}", id))
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(CentreEtatCivil.class));
    }

    public List<RegistrePapier> registresPapier(Long centreId, String typeActe, Integer annee) {
        return appeler(() -> http.get().uri(u -> {
            u.path("/api/registre/registres-papier");
            if (centreId != null) u.queryParam("centre", centreId);
            if (typeActe != null && !typeActe.isBlank()) u.queryParam("typeActe", typeActe);
            if (annee != null) u.queryParam("annee", annee);
            return u.build();
        }).retrieve().body(new ParameterizedTypeReference<List<RegistrePapier>>() {}));
    }

    public RegistrePapier registrePapier(long id) {
        return appeler(() -> http.get().uri("/api/registre/registres-papier/{id}", id).retrieve().body(RegistrePapier.class));
    }

    public RegistrePapier enregistrerRegistrePapier(Long id, Map<String, Object> corps) {
        return appeler(() -> (id == null ? http.post().uri("/api/registre/registres-papier")
                : http.put().uri("/api/registre/registres-papier/{id}", id))
                .contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().body(RegistrePapier.class));
    }

    /** Dossier de renseignement de la demande, ou vide s'il n'a pas encore été ouvert. */
    public Optional<Renseignement> renseignement(UUID demandeId) {
        return Optional.ofNullable(appeler(() -> http.get().uri("/api/demandes/{id}/renseignement", demandeId).retrieve()
                .body(Renseignement.class)));
    }

    public Renseignement actionRenseignement(UUID demandeId, String action, Map<String, Object> corps) {
        return appeler(() -> {
            var r = http.post().uri("/api/demandes/{id}/renseignement/{action}", demandeId, action);
            if (corps != null) r.contentType(MediaType.APPLICATION_JSON).body(corps);
            return r.retrieve().body(Renseignement.class);
        });
    }

    public List<ResumeRenseignement> renseignementsEnCours() {
        return get("/api/demandes/renseignements", new ParameterizedTypeReference<>() {});
    }

    public StatistiquesRenseignement statistiquesRenseignement() {
        return appeler(() -> http.get().uri("/api/demandes/renseignements/statistiques").retrieve()
                .body(StatistiquesRenseignement.class));
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

    // ---------------------------------------------------------------- habilitations

    private static final String HAB = "/api/habilitations";

    public Synthese syntheseHabilitations() {
        return appeler(() -> http.get().uri(HAB + "/synthese").retrieve().body(Synthese.class));
    }

    public Page<Operation> journal(String recherche, int page) {
        return appeler(() -> http.get().uri(u -> {
            if (recherche != null && !recherche.isBlank()) u.queryParam("recherche", recherche.trim());
            return u.path(HAB + "/journal").queryParam("page", page).build();
        }).retrieve().body(new ParameterizedTypeReference<Page<Operation>>() {}));
    }

    public Page<Compte> comptes(String recherche, int page) {
        return appeler(() -> http.get().uri(u -> {
            if (recherche != null && !recherche.isBlank()) u.queryParam("recherche", recherche.trim());
            return u.path(HAB + "/utilisateurs").queryParam("page", page).build();
        }).retrieve().body(new ParameterizedTypeReference<Page<Compte>>() {}));
    }

    public FicheCompte compte(String id) {
        return appeler(() -> http.get().uri(HAB + "/utilisateurs/{id}", id).retrieve().body(FicheCompte.class));
    }

    public String creerCompte(String email, String prenom, String nom, Set<String> profils, String motDePasse) {
        var corps = new HashMap<String, Object>();
        corps.put("email", email);
        corps.put("prenom", prenom);
        corps.put("nom", nom);
        corps.put("profils", profils);
        corps.put("motDePasse", motDePasse);
        Map<?, ?> r = appeler(() -> http.post().uri(HAB + "/utilisateurs").contentType(MediaType.APPLICATION_JSON)
                .body(corps).retrieve().body(Map.class));
        return (String) r.get("id");
    }

    public void activerCompte(String id, boolean actif) {
        envoyer(() -> http.post().uri(HAB + "/utilisateurs/{id}/activation", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("actif", actif)).retrieve().toBodilessEntity());
    }

    public void motDePasseProvisoire(String id, String motDePasse) {
        envoyer(() -> http.post().uri(HAB + "/utilisateurs/{id}/mot-de-passe", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("motDePasse", motDePasse == null ? "" : motDePasse)).retrieve().toBodilessEntity());
    }

    public void profilsDuCompte(String id, Set<String> profils) {
        envoyer(() -> http.put().uri(HAB + "/utilisateurs/{id}/profils", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("profils", profils)).retrieve().toBodilessEntity());
    }

    public List<Profil> profils() {
        return get(HAB + "/profils", new ParameterizedTypeReference<>() {});
    }

    public Profil profil(String id) {
        return appeler(() -> http.get().uri(HAB + "/profils/{id}", id).retrieve().body(Profil.class));
    }

    public String creerProfil(String nom, String description, Set<String> roles) {
        Map<?, ?> r = appeler(() -> http.post().uri(HAB + "/profils").contentType(MediaType.APPLICATION_JSON)
                .body(profilCorps(nom, description, roles)).retrieve().body(Map.class));
        return (String) r.get("id");
    }

    public void modifierProfil(String id, String nom, String description, Set<String> roles) {
        envoyer(() -> http.put().uri(HAB + "/profils/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .body(profilCorps(nom, description, roles)).retrieve().toBodilessEntity());
    }

    public void supprimerProfil(String id) {
        envoyer(() -> http.delete().uri(HAB + "/profils/{id}", id).retrieve().toBodilessEntity());
    }

    public List<Role> roles() {
        return get(HAB + "/roles", new ParameterizedTypeReference<>() {});
    }

    public Role role(String code) {
        return appeler(() -> http.get().uri(HAB + "/roles/{code}", code).retrieve().body(Role.class));
    }

    public String creerRole(String code, String libelle, String description, Set<String> permissions) {
        Map<?, ?> r = appeler(() -> http.post().uri(HAB + "/roles").contentType(MediaType.APPLICATION_JSON)
                .body(roleCorps(code, libelle, description, permissions)).retrieve().body(Map.class));
        return (String) r.get("id");
    }

    public void modifierRole(String code, String libelle, String description, Set<String> permissions) {
        envoyer(() -> http.put().uri(HAB + "/roles/{code}", code).contentType(MediaType.APPLICATION_JSON)
                .body(roleCorps(code, libelle, description, permissions)).retrieve().toBodilessEntity());
    }

    public void supprimerRole(String code) {
        envoyer(() -> http.delete().uri(HAB + "/roles/{code}", code).retrieve().toBodilessEntity());
    }

    public void modifierMatrice(Map<String, Set<String>> permissionsParRole) {
        envoyer(() -> http.put().uri(HAB + "/matrice").contentType(MediaType.APPLICATION_JSON).body(permissionsParRole)
                .retrieve().toBodilessEntity());
    }

    public List<Permission> permissions() {
        return get(HAB + "/permissions", new ParameterizedTypeReference<>() {});
    }

    public void modifierPermission(String code, String libelle, String description, String avantage, String interdiction) {
        var corps = new HashMap<String, Object>();
        corps.put("libelle", libelle);
        corps.put("description", description);
        corps.put("avantage", avantage);
        corps.put("interdiction", interdiction);
        envoyer(() -> http.put().uri(HAB + "/permissions/{code}", code).contentType(MediaType.APPLICATION_JSON)
                .body(corps).retrieve().toBodilessEntity());
    }

    private static Map<String, Object> profilCorps(String nom, String description, Set<String> roles) {
        var corps = new HashMap<String, Object>();
        corps.put("nom", nom);
        corps.put("description", description);
        corps.put("roles", roles == null ? Set.of() : roles);
        return corps;
    }

    private static Map<String, Object> roleCorps(String code, String libelle, String description, Set<String> permissions) {
        var corps = new HashMap<String, Object>();
        corps.put("code", code);
        corps.put("libelle", libelle);
        corps.put("description", description);
        corps.put("permissions", permissions == null ? Set.of() : permissions);
        return corps;
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
