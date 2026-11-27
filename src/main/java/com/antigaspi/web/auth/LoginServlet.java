package com.antigaspi.web.auth;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.AccesUtil;
import com.antigaspi.service.AuthService;
import com.antigaspi.service.impl.AuthServiceImpl;
import com.antigaspi.web.FlashUtil;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/auth/login.jsp";

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur connecte = SessionUtil.utilisateurConnecte(req);
        if (connecte != null) {
            resp.sendRedirect(req.getContextPath() + AccesUtil.accueil(connecte.getRole()));
            return;
        }
        req.getRequestDispatcher(VUE).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String motDePasse = req.getParameter("motDePasse");
        try {
            Utilisateur utilisateur = authService.login(email, motDePasse);
            SessionUtil.connecter(req, utilisateur);
            resp.sendRedirect(req.getContextPath() + cibleApresConnexion(req, utilisateur));
        } catch (BusinessException e) {
            req.setAttribute(FlashUtil.ERREUR, e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de la connexion", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Page que l'utilisateur voulait ouvrir avant la connexion, sinon sa page d'accueil. */
    private String cibleApresConnexion(HttpServletRequest req, Utilisateur utilisateur) {
        String demandee = SessionUtil.retirerUrlDemandee(req);
        if (demandee != null && demandee.startsWith("/") && !demandee.startsWith("//")) {
            String chemin = demandee.contains("?") ? demandee.substring(0, demandee.indexOf('?')) : demandee;
            Role requis = AccesUtil.roleRequis(chemin);
            if (requis == null || requis == utilisateur.getRole()) {
                return demandee;
            }
        }
        return AccesUtil.accueil(utilisateur.getRole());
    }
}
