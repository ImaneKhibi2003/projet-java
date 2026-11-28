package com.antigaspi.web.admin;

import com.antigaspi.service.StatsService;
import com.antigaspi.service.UtilisateurService;
import com.antigaspi.service.impl.StatsServiceImpl;
import com.antigaspi.service.impl.UtilisateurServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Tableau de bord de l'administrateur : statistiques globales et liste des utilisateurs. */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminDashboardServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/admin/dashboard.jsp";

    private final StatsService statsService = new StatsServiceImpl();
    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("stats", statsService.statsAdmin());
            req.setAttribute("utilisateurs", utilisateurService.listerTous());
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage du tableau de bord admin", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
