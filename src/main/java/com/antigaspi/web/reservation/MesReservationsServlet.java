package com.antigaspi.web.reservation;

import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.ReservationService;
import com.antigaspi.service.impl.ReservationServiceImpl;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Réservations du client connecté, avec leurs codes de retrait. */
@WebServlet("/client/reservations")
public class MesReservationsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(MesReservationsServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/reservation/mes.jsp";

    private final ReservationService reservationService = new ReservationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur client = SessionUtil.utilisateurConnecte(req);
        if (client == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("reservations", reservationService.listerParClient(client.getId()));
            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage des réservations", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
