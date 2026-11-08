package sn.etatcivil.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import sn.etatcivil.dao.*;

public class TestServeurServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                out.println("<h2 style='color:green;'>✅ Serveur central connecté à la base PostgreSQL</h2>");
            } else {
                out.println("<h2 style='color:red;'>❌ Échec de connexion à la base</h2>");
            }
        } catch (SQLException e) {
            e.printStackTrace(out);
        }
    }
}
