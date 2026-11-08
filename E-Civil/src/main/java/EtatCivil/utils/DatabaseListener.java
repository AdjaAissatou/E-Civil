package EtatCivil.utils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebListener
public class DatabaseListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("DatabaseListener: Initialisation du pool de connexions PostgreSQL");
        
        // Test de connexion au démarrage
        try {
            Connection testConnection = getConnection();
            if (testConnection != null && !testConnection.isClosed()) {
                System.out.println("DatabaseListener: Test de connexion réussi");
                testConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("DatabaseListener: ERREUR - Test de connexion échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("DatabaseListener: Arrêt de l'application");
    }
    
    // Méthode statique pour obtenir une nouvelle connexion
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/E-Civil";
            String user = "postgres";
            String password = "Dione2003"; // Remplacez par votre vrai mot de passe
            
            System.out.println("DatabaseListener: Tentative de connexion à: " + url);
            Connection connection = DriverManager.getConnection(url, user, password);
            
            if (connection != null) {
                System.out.println("DatabaseListener: Connexion établie avec succès");
            } else {
                System.out.println("DatabaseListener: ERREUR - Connexion null");
            }
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            System.err.println("DatabaseListener: Driver PostgreSQL non trouvé");
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        } catch (SQLException e) {
            System.err.println("DatabaseListener: Erreur SQL lors de la connexion: " + e.getMessage());
            throw e;
        }
    }
}