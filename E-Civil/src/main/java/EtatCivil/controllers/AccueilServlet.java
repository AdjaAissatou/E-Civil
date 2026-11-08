package EtatCivil.controllers;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "AccueilServlet", urlPatterns = {"/", "/accueil"})
public class AccueilServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("title", "Accueil - E-Civil");
        req.setAttribute("pageContent", "/WEB-INF/views/accueil.jsp");
        System.out.println("pageContent = " + req.getAttribute("pageContent"));
        req.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(req, resp);
    }
}