package com.antigaspi.web.panier;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.model.Role;
import com.antigaspi.model.StatutPanier;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.PanierService;
import com.antigaspi.service.impl.PanierServiceImpl;
import com.antigaspi.web.FlashUtil;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Détail public d'un panier, avec le formulaire de réservation (réservé aux clients connectés). */
@WebServlet("/panier")
public class PanierDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PanierDetailServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/panier/detail.jsp";

    private final PanierService panierService = new PanierServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Panier panier = panierService.trouverParId(lireId(req.getParameter("id")));
            Utilisateur connecte = SessionUtil.utilisateurConnecte(req);

            String raison = raisonNonReservable(panier, connecte);
            req.setAttribute("panier", panier);
            req.setAttribute("reservable", raison == null);
            req.setAttribute("messageReservation", raison);
            req.setAttribute("maxQuantite", panier.getQuantiteRestante());
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/paniers");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage d'un panier", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** null si l'utilisateur peut réserver, sinon la raison à afficher à la place du bouton. */
    private static String raisonNonReservable(Panier panier, Utilisateur connecte) {
        if (panier.getStatut() != StatutPanier.DISPONIBLE) {
            return "Ce panier n'est plus disponible";
        }
        if (panier.getQuantiteRestante() <= 0) {
            return "Il ne reste plus de panier";
        }
        if (panier.getDateFinRetrait().isBefore(LocalDateTime.now())) {
            return "La période de retrait est terminée";
        }
        if (connecte == null) {
            return "Connectez-vous pour réserver";
        }
        if (connecte.getRole() != Role.CLIENT) {
            return "Seuls les clients peuvent réserver";
        }
        return null;
    }

    private static Long lireId(String valeur) {
        try {
            return valeur == null ? null : Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
