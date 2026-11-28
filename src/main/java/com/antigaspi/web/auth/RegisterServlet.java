package com.antigaspi.web.auth;

import com.antigaspi.exception.BusinessException;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RegisterServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/auth/register.jsp";

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
        String nom = req.getParameter("nom");
        String prenom = req.getParameter("prenom");
        String email = req.getParameter("email");
        String motDePasse = req.getParameter("motDePasse");
        String confirmation = req.getParameter("confirmation");
        try {
            if (motDePasse == null || !motDePasse.equals(confirmation)) {
                throw new BusinessException("Les deux mots de passe ne sont pas identiques");
            }
            authService.register(nom, prenom, email, motDePasse);
            FlashUtil.succes(req, "Compte créé avec succès : vous pouvez vous connecter");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (BusinessException e) {
            req.setAttribute(FlashUtil.ERREUR, e.getMessage());
            req.setAttribute("nom", nom);
            req.setAttribute("prenom", prenom);
            req.setAttribute("email", email);
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'inscription", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
