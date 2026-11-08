package EtatCivil.dao;

import EtatCivil.config.ApiConfig;
import EtatCivil.modeles.ActeRegistre;
import EtatCivil.services.ServeurCentralClient;
import EtatCivil.services.ServeurCentralClient.ApiStatistiques;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ActeRegistreDAO {
    private final ServeurCentralClient apiClient;
    private final RestTemplate restTemplate;

    public ActeRegistreDAO() {
        this.restTemplate = new RestTemplate();
        this.apiClient = new ServeurCentralClient();
    }

    public boolean creerNouvelActe(ActeRegistre acte) {
        try {
            return apiClient.creerNouvelActe(acte);
        } catch (Exception e) {
            throw new RuntimeException("Erreur création Acte via API", e);
        }
    }

    public ApiStatistiques getStatistiques() {
        try {
            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath() + "/statistiques";
            ResponseEntity<ApiStatistiques> response = restTemplate.getForEntity(url, ApiStatistiques.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                return createDefaultStats();
            }
        } catch (Exception e) {
            return createDefaultStats();
        }
    }

    private ApiStatistiques createDefaultStats() {
        ApiStatistiques stats = new ApiStatistiques();
        stats.setTotalDemandes(0);
        stats.setEnAttente(0);
        stats.setCompletes(0);
        stats.setNonTrouves(0);
        return stats;
    }

    public void close() {
        // Rien à fermer
    }
}
