package sn.etatcivil.services;

import sn.etatcivil.Modeles.ActeRegistre;
import sn.etatcivil.repositories.ActeRegistreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActeRegistreService {
    
    @Autowired
    private ActeRegistreRepository repository;
    
    public ActeRegistre ajouterActe(ActeRegistre acte) {
        return repository.save(acte);
    }
    
    public long countActes() {
        return repository.count();
    }
    
    public List<ActeRegistre> getActesAvecFiltres(String typeActe, String statut, String dateDebut, String dateFin) {
        System.out.println("🔍 Service: Recherche actes avec filtres");
        System.out.println("   Type: " + typeActe + ", Statut: " + statut);
        
        LocalDateTime debut = dateDebut != null ? LocalDateTime.parse(dateDebut + "T00:00:00") : null;
        LocalDateTime fin = dateFin != null ? LocalDateTime.parse(dateFin + "T23:59:59") : null;
        
        List<ActeRegistre> result = repository.findActesWithFilters(typeActe, statut, debut, fin);
        System.out.println("✅ Service: " + result.size() + " actes trouvés");
        return result;
    }
    
    public ActeRegistre creerNouvelActe(ActeRegistre acte) {
        System.out.println("📝 Service: Création nouvel acte de type: " + acte.getTypeActe());
        
        // Générer les numéros automatiquement
        genererNumeros(acte);
        
        // Validation : vérifier si le numéro d'acte existe déjà
        if (acte.getNumeroActe() != null && acte.getAnnee() != null) {
            boolean existeDeja = repository.existsByNumeroActeAndAnnee(acte.getNumeroActe(), acte.getAnnee());
            if (existeDeja) {
                System.err.println("❌ Service: Numéro d'acte " + acte.getNumeroActe() + " existe déjà pour l'année " + acte.getAnnee());
                throw new RuntimeException("Un acte avec ce numéro existe déjà pour cette année");
            }
        }
        
        // Définir les dates automatiquement
        acte.setDateSaisie(LocalDateTime.now());
        acte.setDateModification(LocalDateTime.now());
        
        if (acte.getStatut() == null) {
            acte.setStatut("saisi");
        }
        
        // Log des données avant sauvegarde
        System.out.println("💾 Données à sauvegarder:");
        System.out.println("   - Numéro Acte: " + acte.getNumeroActe());
        System.out.println("   - Numéro Personne: " + acte.getNumeroPersonne());
        System.out.println("   - Numéro Couple: " + acte.getNumeroCouple());
        System.out.println("   - Année: " + acte.getAnnee());
        System.out.println("   - Statut: " + acte.getStatut());
        
        // Sauvegarder dans la base de données
        ActeRegistre saved = repository.save(acte);
        
        System.out.println("✅ Service: Acte créé avec ID: " + saved.getId() + 
                          ", Numéro Acte: " + saved.getNumeroActe() +
                          ", Numéro Personne: " + saved.getNumeroPersonne() +
                          ", Numéro Couple: " + saved.getNumeroCouple());
        return saved;
    }
    
    private void genererNumeros(ActeRegistre acte) {
        String anneeCourante = String.valueOf(Year.now().getValue());
        acte.setAnnee(anneeCourante);
        
        // Générer le numéro d'acte principal
        String prefixeType = getPrefixeType(acte.getTypeActe());
        long countActesAnnee = repository.countByAnnee(anneeCourante);
        String numeroActe = String.format("%s-%s-%04d", prefixeType, anneeCourante, countActesAnnee + 1);
        acte.setNumeroActe(numeroActe);
        
        // Gestion des numéros de personne et de couple selon le type d'acte
        if (acte.getTypeActe() != null) {
            switch (acte.getTypeActe().toLowerCase()) {
                case "naissance":
                    genererNumeroPersonne(acte);
                    break;
                case "mariage":
                case "divorce":
                    genererNumeroCouple(acte);
                    break;
                case "décès":
                    genererNumeroPersonne(acte);
                    break;
                default:
                    genererNumeroPersonne(acte);
                    break;
            }
        }
    }
    
    private String getPrefixeType(String typeActe) {
        if (typeActe == null) return "ACT";
        switch (typeActe.toLowerCase()) {
            case "naissance": return "N";
            case "mariage": return "M";
            case "divorce": return "D";
            case "décès": return "DEC";
            default: return "ACT";
        }
    }
    
    private void genererNumeroPersonne(ActeRegistre acte) {
        // Pour l'instant, générer un numéro séquentiel
        // Dans un système complet, on chercherait si la personne existe déjà
        long countPersonnes = repository.count();
        String numeroPersonne = String.format("P%06d", countPersonnes + 1);
        acte.setNumeroPersonne(numeroPersonne);
        System.out.println("   🔢 Numéro personne généré: " + numeroPersonne);
    }
    
    private void genererNumeroCouple(ActeRegistre acte) {
        // Pour l'instant, générer un numéro séquentiel
        // Dans un système complet, on chercherait si le couple existe déjà
        long countCouples = repository.countByNumeroCoupleIsNotNull();
        String numeroCouple = String.format("C%06d", countCouples + 1);
        acte.setNumeroCouple(numeroCouple);
        System.out.println("   🔢 Numéro couple généré: " + numeroCouple);
        
        // Générer aussi le numéro de personne pour les conjoints
        genererNumeroPersonne(acte);
    }
    
    public Optional<ActeRegistre> modifierActe(Integer id, ActeRegistre nouvellesDonnees) {
        System.out.println("✏️ Service: Modification acte ID: " + id);
        
        return repository.findById(id).map(acteExistant -> {
            // Journaliser les modifications
            System.out.println("   Modification acte ID " + id + " - Ancien statut: " + acteExistant.getStatut());
            
            // Mettre à jour les champs modifiables
            if (nouvellesDonnees.getTypeActe() != null) {
                acteExistant.setTypeActe(nouvellesDonnees.getTypeActe());
            }
            if (nouvellesDonnees.getNom() != null) {
                acteExistant.setNom(nouvellesDonnees.getNom());
            }
            if (nouvellesDonnees.getPrenom() != null) {
                acteExistant.setPrenom(nouvellesDonnees.getPrenom());
            }
            if (nouvellesDonnees.getDateNaissance() != null) {
                acteExistant.setDateNaissance(nouvellesDonnees.getDateNaissance());
            }
            if (nouvellesDonnees.getLieuNaissance() != null) {
                acteExistant.setLieuNaissance(nouvellesDonnees.getLieuNaissance());
            }
            if (nouvellesDonnees.getNomPere() != null) {
                acteExistant.setNomPere(nouvellesDonnees.getNomPere());
            }
            if (nouvellesDonnees.getPrenomPere() != null) {
                acteExistant.setPrenomPere(nouvellesDonnees.getPrenomPere());
            }
            if (nouvellesDonnees.getNomMere() != null) {
                acteExistant.setNomMere(nouvellesDonnees.getNomMere());
            }
            if (nouvellesDonnees.getPrenomMere() != null) {
                acteExistant.setPrenomMere(nouvellesDonnees.getPrenomMere());
            }
            if (nouvellesDonnees.getNomConjoint() != null) {
                acteExistant.setNomConjoint(nouvellesDonnees.getNomConjoint());
            }
            if (nouvellesDonnees.getPrenomConjoint() != null) {
                acteExistant.setPrenomConjoint(nouvellesDonnees.getPrenomConjoint());
            }
            if (nouvellesDonnees.getDateNaissanceConjoint() != null) {
                acteExistant.setDateNaissanceConjoint(nouvellesDonnees.getDateNaissanceConjoint());
            }
            if (nouvellesDonnees.getLieuMariage() != null) {
                acteExistant.setLieuMariage(nouvellesDonnees.getLieuMariage());
            }
            if (nouvellesDonnees.getTemoin1() != null) {
                acteExistant.setTemoin1(nouvellesDonnees.getTemoin1());
            }
            if (nouvellesDonnees.getTemoin2() != null) {
                acteExistant.setTemoin2(nouvellesDonnees.getTemoin2());
            }
            if (nouvellesDonnees.getDateDeces() != null) {
                acteExistant.setDateDeces(nouvellesDonnees.getDateDeces());
            }
            if (nouvellesDonnees.getHeureDeces() != null) {
                acteExistant.setHeureDeces(nouvellesDonnees.getHeureDeces());
            }
            if (nouvellesDonnees.getCauseDeces() != null) {
                acteExistant.setCauseDeces(nouvellesDonnees.getCauseDeces());
            }
            if (nouvellesDonnees.getLieuDeces() != null) {
                acteExistant.setLieuDeces(nouvellesDonnees.getLieuDeces());
            }
            if (nouvellesDonnees.getNumeroActe() != null) {
                acteExistant.setNumeroActe(nouvellesDonnees.getNumeroActe());
            }
            if (nouvellesDonnees.getVolume() != null) {
                acteExistant.setVolume(nouvellesDonnees.getVolume());
            }
            if (nouvellesDonnees.getFolio() != null) {
                acteExistant.setFolio(nouvellesDonnees.getFolio());
            }
            if (nouvellesDonnees.getAnnee() != null) {
                acteExistant.setAnnee(nouvellesDonnees.getAnnee());
            }
            if (nouvellesDonnees.getCentre() != null) {
                acteExistant.setCentre(nouvellesDonnees.getCentre());
            }
            if (nouvellesDonnees.getObservations() != null) {
                acteExistant.setObservations(nouvellesDonnees.getObservations());
            }
            if (nouvellesDonnees.getStatut() != null) {
                acteExistant.setStatut(nouvellesDonnees.getStatut());
                System.out.println("   Nouveau statut: " + nouvellesDonnees.getStatut());
            }
            
            acteExistant.setDateModification(LocalDateTime.now());
            if (nouvellesDonnees.getIdAgent() != null) {
                acteExistant.setIdAgent(nouvellesDonnees.getIdAgent());
            }
            
            ActeRegistre updated = repository.save(acteExistant);
            System.out.println("✅ Service: Acte ID " + id + " modifié et sauvegardé");
            return updated;
        });
    }
    
    public boolean supprimerActe(Integer id) {
        System.out.println("🗑️ Service: Suppression acte ID: " + id);
        
        if (repository.existsById(id)) {
            repository.deleteById(id);
            System.out.println("✅ Service: Acte ID " + id + " supprimé de la base");
            return true;
        }
        
        System.err.println("❌ Service: Acte ID " + id + " non trouvé pour suppression");
        return false;
    }
    
    public Statistiques getStatistiques() {
        System.out.println("📊 Service: Calcul des statistiques depuis la base");
        
        try {
            Statistiques stats = new Statistiques();
            stats.setTotalActes(repository.count());
            stats.setActesSaisis(repository.countSaisis());
            stats.setActesVerifies(repository.countVerifies());
            stats.setActesValides(repository.countValides());
            
            System.out.println("📊 Statistiques base: Total=" + stats.getTotalActes() + 
                             ", Saisis=" + stats.getActesSaisis() +
                             ", Vérifiés=" + stats.getActesVerifies() +
                             ", Validés=" + stats.getActesValides());
            
            return stats;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du calcul des statistiques: " + e.getMessage());
            // Retourner des statistiques par défaut en cas d'erreur
            Statistiques defaultStats = new Statistiques();
            defaultStats.setTotalActes(0);
            defaultStats.setActesSaisis(0);
            defaultStats.setActesVerifies(0);
            defaultStats.setActesValides(0);
            return defaultStats;
        }
    }
    
    public static class Statistiques {
        private long totalActes;
        private long actesSaisis;
        private long actesVerifies;
        private long actesValides;
        
        public long getTotalActes() { return totalActes; }
        public void setTotalActes(long totalActes) { this.totalActes = totalActes; }
        public long getActesSaisis() { return actesSaisis; }
        public void setActesSaisis(long actesSaisis) { this.actesSaisis = actesSaisis; }
        public long getActesVerifies() { return actesVerifies; }
        public void setActesVerifies(long actesVerifies) { this.actesVerifies = actesVerifies; }
        public long getActesValides() { return actesValides; }
        public void setActesValides(long actesValides) { this.actesValides = actesValides; }
    }
}