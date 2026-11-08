package EtatCivil.controllers;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "TestConnexionServlet", value = "/test-connexion")
public class TestConnexionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        Connection connection = (Connection) getServletContext().getAttribute("connexion");
        
        out.println("<html><body>");
        out.println("<h2>Test de connexion PostgreSQL</h2>");
        
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    out.println("<p style='color: green;'>Connexion PostgreSQL établie avec succès!</p>");
                    out.println("<p>Base de données: " + connection.getMetaData().getDatabaseProductName() + "</p>");
                    out.println("<p>Version: " + connection.getMetaData().getDatabaseProductVersion() + "</p>");
                } else {
                    out.println("<p style='color: red;'>Connexion PostgreSQL fermée</p>");
                }
            } catch (SQLException e) {
                out.println("<p style='color: red;'>Erreur lors de la vérification de la connexion: " + e.getMessage() + "</p>");
            }
        } else {
            out.println("<p style='color: red;'>Aucune connexion PostgreSQL disponible</p>");
        }
        
        out.println("</body></html>");
    }
}