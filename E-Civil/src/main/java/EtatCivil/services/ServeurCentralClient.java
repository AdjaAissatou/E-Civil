package EtatCivil.services;

import EtatCivil.config.ApiConfig;
import EtatCivil.modeles.ActeRegistre;
import EtatCivil.modeles.DemandeDocument;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class ServeurCentralClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ServeurCentralClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    public static class ApiStatistiques {
        private int totalDemandes;
        private int enAttente;
        private int completes;
        private int nonTrouves;
        
        // Getters et Setters
        public int getTotalDemandes() { return totalDemandes; }
        public void setTotalDemandes(int totalDemandes) { this.totalDemandes = totalDemandes; }
        
        public int getEnAttente() { return enAttente; }
        public void setEnAttente(int enAttente) { this.enAttente = enAttente; }
        
        public int getCompletes() { return completes; }
        public void setCompletes(int completes) { this.completes = completes; }
        
        public int getNonTrouves() { return nonTrouves; }
        public void setNonTrouves(int nonTrouves) { this.nonTrouves = nonTrouves; }
    }
    
    public ApiStatistiques getStatistiques() {
        try {
            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath() + "/statistiques";
            System.out.println("📡 Appel API Statistiques: " + url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("📡 Réponse statistiques - Status: " + response.getStatusCode());
            System.out.println("📡 Réponse statistiques - Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Convertir le JSON en objet ApiStatistiques
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), 
                    new TypeReference<Map<String, Object>>() {});
                
                ApiStatistiques stats = new ApiStatistiques();
                stats.setTotalDemandes((Integer) responseMap.getOrDefault("totalDemandes", 0));
                stats.setEnAttente((Integer) responseMap.getOrDefault("enAttente", 0));
                stats.setCompletes((Integer) responseMap.getOrDefault("completes", 0));
                stats.setNonTrouves((Integer) responseMap.getOrDefault("nonTrouves", 0));
                
                return stats;
            } else {
                System.err.println("❌ Erreur API: Statut " + response.getStatusCode());
                return createDefaultStats();
            }
        } catch (Exception e) {
            System.err.println("❌ ERREUR API statistiques: " + e.getMessage());
            e.printStackTrace();
            return createDefaultStats();
        }
    }
    
    public boolean creerNouvelActe(ActeRegistre acte) {
        try {
            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath();
            System.out.println("📡 Création nouvel acte - URL: " + url);

            // Initialiser ObjectMapper avec module JavaTime pour gérer les dates
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Convertir l'objet ActeRegistre directement en JSON
            String jsonPayload = mapper.writeValueAsString(acte);

            System.out.println("📦 JSON envoyé: " + jsonPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("📡 Réponse création acte - Status: " + response.getStatusCode());
            System.out.println("📡 Réponse création acte - Body: " + response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ ERREUR API création acte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private ApiStatistiques createDefaultStats() {
        ApiStatistiques defaultStats = new ApiStatistiques();
        defaultStats.setTotalDemandes(0);
        defaultStats.setEnAttente(0);
        defaultStats.setCompletes(0);
        defaultStats.setNonTrouves(0);
        return defaultStats;
    }

    /**
     * Méthode pour lancer une recherche d'acte dans le serveur central
     * Cette méthode est appelée quand une demande passe en statut "recherché"
     */
    public boolean lancerRechercheActe(DemandeDocument demande) {
        try {
            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath() + "/rechercher";
            System.out.println("🔍 Lancement recherche acte - URL: " + url);
            
            // Préparer les données de recherche
            Map<String, Object> rechercheData = new HashMap<>();
            rechercheData.put("idDemande", demande.getIdDemande());
            rechercheData.put("nom", demande.getNom());
            rechercheData.put("prenom", demande.getPrenom());
            rechercheData.put("dateNaissance", demande.getDatedenaissance());
            rechercheData.put("lieuNaissance", demande.getLieudenaissance());
            rechercheData.put("typeDocument", demande.getNomDocument());
            rechercheData.put("format", demande.getFormat());
            
            // Ajouter les informations spécifiques selon le type de document
            if (demande.getInformationsSupplementaires() != null) {
                rechercheData.putAll(demande.getInformationsSupplementaires());
            }
            
            System.out.println("📦 Données de recherche: " + rechercheData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonPayload = objectMapper.writeValueAsString(rechercheData);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("📡 Réponse recherche - Status: " + response.getStatusCode());
            System.out.println("📡 Réponse recherche - Body: " + response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la recherche d'acte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier le statut d'une recherche en cours
     */
    public Map<String, Object> verifierStatutRecherche(Long idDemande) {
        try {
            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath() + "/statut-recherche/" + idDemande;
            System.out.println("🔍 Vérification statut recherche - URL: " + url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR vérification statut recherche: " + e.getMessage());
        }
        
        // Retourner un statut par défaut en cas d'erreur
        Map<String, Object> statutDefaut = new HashMap<>();
        statutDefaut.put("statut", "erreur");
        statutDefaut.put("message", "Impossible de contacter le serveur central");
        return statutDefaut;
    }
}