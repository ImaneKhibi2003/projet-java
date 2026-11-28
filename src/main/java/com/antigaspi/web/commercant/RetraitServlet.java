package com.antigaspi.web.commercant;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.Retrait;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.RetraitService;
import com.antigaspi.service.impl.RetraitServiceImpl;
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

/**
 * GET  /commercant/retrait : formulaire de saisie du code + retraits du jour
 * POST /commercant/retrait : valide le code saisi (message de succès ou d'erreur, puis retour au formulaire)
 */
@WebServlet("/commercant/retrait")
public class RetraitServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RetraitServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/commercant/valider-retrait.jsp";

    private final RetraitService retraitService = new RetraitServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur commercant = SessionUtil.utilisateurConnecte(req);
        if (commercant == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("retraitsDuJour", retraitService.listerRetraitsDuJour(commercant.getId()));
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage des retraits", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur commercant = SessionUtil.utilisateurConnecte(req);
        if (commercant == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            // le commerçant est celui de la session, jamais un identifiant envoyé par le formulaire
            Retrait retrait = retraitService.validerRetrait(commercant.getId(), req.getParameter("codeRetrait"));
            Reservation r = retrait.getReservation();
            FlashUtil.succes(req, "Retrait validé : " + r.getQuantite() + " x " + r.getPanier().getTitre()
                    + " remis à " + r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom());
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de la validation d'un retrait", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/commercant/retrait");
    }
}
