package EtatCivil.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/E-Civil"; 
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "Dione2003"; 

    public static Connection getConnexion() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
