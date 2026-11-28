package com.antigaspi.web.commercant;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.CommerceService;
import com.antigaspi.service.PanierService;
import com.antigaspi.service.ReservationService;
import com.antigaspi.service.StatsService;
import com.antigaspi.service.impl.CommerceServiceImpl;
import com.antigaspi.service.impl.PanierServiceImpl;
import com.antigaspi.service.impl.ReservationServiceImpl;
import com.antigaspi.service.impl.StatsServiceImpl;
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

/** Tableau de bord du commerçant : statistiques, ses paniers et les réservations reçues. */
@WebServlet("/commercant/dashboard")
public class CommercantDashboardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CommercantDashboardServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/commercant/dashboard.jsp";

    private final CommerceService commerceService = new CommerceServiceImpl();
    private final PanierService panierService = new PanierServiceImpl();
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final StatsService statsService = new StatsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur commercant = SessionUtil.utilisateurConnecte(req);
        if (commercant == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            Commerce commerce = commerceService.trouverParProprietaire(commercant.getId());
            req.setAttribute("commerce", commerce);
            req.setAttribute("stats", statsService.statsCommercant(commercant.getId()));
            req.setAttribute("paniers", panierService.listerParCommerce(commerce.getId()));
            req.setAttribute("reservations", reservationService.listerParCommerce(commerce.getId()));
        } catch (BusinessException e) {
            // compte commerçant sans commerce : la page affiche seulement ce message
            req.setAttribute(FlashUtil.ERREUR, e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage du tableau de bord", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        req.getRequestDispatcher(VUE).forward(req, resp);
    }
}
