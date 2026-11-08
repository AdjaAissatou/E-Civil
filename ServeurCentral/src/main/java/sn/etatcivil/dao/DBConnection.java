package sn.etatcivil.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/serveur-central-etat-civil";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Dione2003";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion réussie au serveur central PostgreSQL");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Pilote PostgreSQL introuvable !");
                e.printStackTrace();
            }
        }
        return connection;
    }
}
