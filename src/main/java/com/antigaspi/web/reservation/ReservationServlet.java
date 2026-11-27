package com.antigaspi.web.reservation;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.ReservationService;
import com.antigaspi.service.impl.ReservationServiceImpl;
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
 * POST /client/reserver  (panierId, quantite)
 * POST /client/annuler   (reservationId)
 * Le client est toujours celui de la session, jamais un identifiant envoyé par le formulaire.
 */
@WebServlet({"/client/reserver", "/client/annuler"})
public class ReservationServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ReservationServlet.class.getName());

    private final ReservationService reservationService = new ReservationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/client/reservations");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur client = SessionUtil.utilisateurConnecte(req);
        if (client == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        boolean reservation = "/client/reserver".equals(req.getServletPath());
        String cibleErreur = req.getContextPath() + "/client/reservations";
        try {
            if (reservation) {
                Long panierId = lireLong(req.getParameter("panierId"), "Panier invalide");
                cibleErreur = req.getContextPath() + "/panier?id=" + panierId;
                int quantite = lireEntier(req.getParameter("quantite"), "Quantité invalide");

                Reservation cree = reservationService.reserver(client.getId(), panierId, quantite);
                FlashUtil.succes(req, "Réservation confirmée ! Votre code de retrait : " + cree.getCodeRetrait());
            } else {
                Long reservationId = lireLong(req.getParameter("reservationId"), "Réservation invalide");
                reservationService.annuler(client.getId(), reservationId);
                FlashUtil.succes(req, "Réservation annulée");
            }
            resp.sendRedirect(req.getContextPath() + "/client/reservations");
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
            resp.sendRedirect(cibleErreur);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de la gestion d'une réservation", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static Long lireLong(String valeur, String messageErreur) {
        try {
            return Long.valueOf(valeur.trim());
        } catch (RuntimeException e) { // null ou format invalide
            throw new BusinessException(messageErreur);
        }
    }

    private static int lireEntier(String valeur, String messageErreur) {
        try {
            return Integer.parseInt(valeur.trim());
        } catch (RuntimeException e) {
            throw new BusinessException(messageErreur);
        }
    }
}
