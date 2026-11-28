package com.antigaspi.web.panier;

import com.antigaspi.model.Panier;
import com.antigaspi.service.PanierService;
import com.antigaspi.service.impl.PanierServiceImpl;
import com.antigaspi.web.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Liste publique des paniers disponibles : filtres (catégorie, prix max), tri et pagination. */
@WebServlet("/paniers")
public class PanierListServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PanierListServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/panier/list.jsp";
    static final int TAILLE_PAGE = 6;

    private final PanierService panierService = new PanierServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String categorie = nettoyer(req.getParameter("categorie"));
            String tri = "prix".equals(req.getParameter("tri")) ? "prix" : "date";

            // prix maximum : une saisie invalide est ignorée avec un message
            String prixSaisi = nettoyer(req.getParameter("prixMax"));
            BigDecimal prixMax = null;
            if (prixSaisi != null) {
                try {
                    prixMax = new BigDecimal(prixSaisi.replace(',', '.'));
                    if (prixMax.signum() < 0) {
                        throw new NumberFormatException("négatif");
                    }
                } catch (NumberFormatException e) {
                    prixMax = null;
                    prixSaisi = null;
                    req.setAttribute(FlashUtil.ERREUR, "Le prix maximum doit être un nombre positif");
                }
            }

            List<Panier> resultats = panierService.rechercher(categorie, prixMax, tri);

            // pagination
            int total = resultats.size();
            int nbPages = Math.max(1, (int) Math.ceil(total / (double) TAILLE_PAGE));
            int page = Math.min(Math.max(1, lireEntier(req.getParameter("page"), 1)), nbPages);
            int debut = (page - 1) * TAILLE_PAGE;
            List<Panier> pagePaniers = resultats.subList(debut, Math.min(total, debut + TAILLE_PAGE));

            req.setAttribute("paniers", pagePaniers);
            req.setAttribute("categories", panierService.listerCategories());
            req.setAttribute("page", page);
            req.setAttribute("nbPages", nbPages);
            req.setAttribute("total", total);
            // valeurs saisies, pour réafficher le formulaire
            req.setAttribute("categorie", categorie);
            req.setAttribute("prixMaxSaisi", prixSaisi);
            req.setAttribute("tri", tri);
            // paramètres à conserver dans les liens de pagination
            req.setAttribute("filtres", construireFiltres(tri, categorie, prixSaisi));

            req.getRequestDispatcher(VUE).forward(req, resp);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur technique lors de l'affichage des paniers", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static String nettoyer(String valeur) {
        if (valeur == null) {
            return null;
        }
        String v = valeur.trim();
        return v.isEmpty() ? null : v;
    }

    private static int lireEntier(String valeur, int parDefaut) {
        try {
            return valeur == null ? parDefaut : Integer.parseInt(valeur.trim());
        } catch (NumberFormatException e) {
            return parDefaut;
        }
    }

    private static String construireFiltres(String tri, String categorie, String prixSaisi) {
        StringBuilder sb = new StringBuilder("tri=").append(tri);
        if (categorie != null) {
            sb.append("&categorie=").append(URLEncoder.encode(categorie, StandardCharsets.UTF_8));
        }
        if (prixSaisi != null) {
            sb.append("&prixMax=").append(URLEncoder.encode(prixSaisi, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
