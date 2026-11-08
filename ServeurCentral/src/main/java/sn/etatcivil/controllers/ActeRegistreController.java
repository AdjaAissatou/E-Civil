package sn.etatcivil.controllers;

import sn.etatcivil.Modeles.ActeRegistre;
import sn.etatcivil.services.ActeRegistreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actes")
@CrossOrigin(origins = "*")
public class ActeRegistreController {
    
    @Autowired
    private ActeRegistreService service;
    
    @GetMapping("/liste")
    public List<ActeRegistre> getActesAvecFiltres(
            @RequestParam(required = false) String typeActe,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        
        System.out.println("📥 GET /api/actes/liste appelé");
        System.out.println("   📋 Paramètres - typeActe: " + typeActe + ", statut: " + statut);
        System.out.println("   📋 Paramètres - dateDebut: " + dateDebut + ", dateFin: " + dateFin);
        
        List<ActeRegistre> result = service.getActesAvecFiltres(typeActe, statut, dateDebut, dateFin);
        System.out.println("✅ GET /api/actes/liste - " + result.size() + " actes retournés");
        return result;
    }
    
    @PostMapping("/nouveau")
    public ResponseEntity<Map<String, Object>> creerNouvelActe(@RequestBody ActeRegistre acte) {
        System.out.println("📥 POST /api/actes/nouveau appelé");
        System.out.println("   📦 Données reçues - Type: " + acte.getTypeActe());
        System.out.println("   📦 Données reçues - Nom: " + acte.getNom() + ", Prénom: " + acte.getPrenom());
        System.out.println("   📦 Données reçues - Centre: " + acte.getCentre());
        
        try {
            ActeRegistre saved = service.creerNouvelActe(acte);
            System.out.println("✅ POST /api/actes/nouveau - Acte créé avec ID: " + saved.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Acte créé avec succès");
            response.put("data", saved);
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            System.err.println("❌ POST /api/actes/nouveau - Erreur: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la création de l'acte: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> creerActe(@RequestBody ActeRegistre acte) {
        System.out.println("📥 POST /api/actes appelé");
        System.out.println("   📦 Données reçues - Type: " + acte.getTypeActe());
        System.out.println("   📦 Données reçues - Nom: " + acte.getNom() + ", Prénom: " + acte.getPrenom());
        
        try {
            ActeRegistre saved = service.creerNouvelActe(acte);
            System.out.println("✅ POST /api/actes - Acte créé avec ID: " + saved.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Acte créé avec succès via endpoint POST /api/actes");
            response.put("data", saved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ POST /api/actes - Erreur: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la création de l'acte: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        System.out.println("📥 GET /api/actes/statistiques appelé");
        
        try {
            ActeRegistreService.Statistiques stats = service.getStatistiques();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalDemandes", stats.getTotalActes());
            response.put("enAttente", stats.getActesSaisis());
            response.put("completes", stats.getActesValides());
            response.put("nonTrouves", 0); // À adapter selon votre logique métier
            
            System.out.println("✅ GET /api/actes/statistiques - Retour: " + response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du calcul des statistiques: " + e.getMessage());
            
            // Retourner des statistiques par défaut en cas d'erreur
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalDemandes", 0);
            defaultStats.put("enAttente", 0);
            defaultStats.put("completes", 0);
            defaultStats.put("nonTrouves", 0);
            
            return ResponseEntity.ok(defaultStats);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        System.out.println("📥 GET /api/actes/health appelé");
        
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ServeurCentral API - Module Actes Registre");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("port", "8081");
        health.put("context", "/ServeurCentral");
        health.put("database", "PostgreSQL");
        
        System.out.println("✅ GET /api/actes/health - Service en ligne");
        return ResponseEntity.ok(health);
    }
    
    // Nouveau endpoint pour tester la connexion à la base de données
    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        System.out.println("📥 GET /api/actes/test-db appelé");
        
        try {
            long count = service.countActes();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Connexion à la base de données réussie");
            response.put("totalActes", count);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("✅ Test base de données réussi - " + count + " actes trouvés");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur connexion base de données: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Erreur de connexion à la base de données: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}