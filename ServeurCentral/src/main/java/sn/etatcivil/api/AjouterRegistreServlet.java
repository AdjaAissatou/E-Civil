//package sn.etatcivil.api;
//
//
//import sn.etatcivil.dao.*;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//
//@WebServlet("/api/registre/add")
//public class AjouterRegistreServlet extends HttpServlet {
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        req.setCharacterEncoding("UTF-8");
//        resp.setContentType("application/json;charset=UTF-8");
//
//        String nom = req.getParameter("nom_personne");
//        String prenom = req.getParameter("prenom_personne");
//        String date = req.getParameter("date_evenement");
//        String lieu = req.getParameter("lieu_evenement");
//        String pays = req.getParameter("pays_enregistrement");
//
//        try (Connection conn = DBConnection.getConnection()) {
//            String sql = "INSERT INTO registre_etat_civil (nom_personne, prenom_personne, date_evenement, lieu_evenement, pays_enregistrement, statut, annee_registre, numero_registre, numero_acte) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setString(1, nom);
//            ps.setString(2, prenom);
//            ps.setDate(3, java.sql.Date.valueOf(date));
//            ps.setString(4, lieu);
//            ps.setString(5, pays);
//            ps.setString(6, "saisi");
//            ps.setInt(7, 2025);
//            ps.setString(8, "REG-2025");
//            ps.setInt(9, (int)(Math.random()*1000));
//            ps.executeUpdate();
//
//            resp.getWriter().println("{\"message\": \"✅ Registre ajouté avec succès !\"}");
//        } catch (Exception e) {
//            resp.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
//        }
//    }
//}
