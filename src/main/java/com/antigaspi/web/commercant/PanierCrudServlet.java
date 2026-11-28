package com.antigaspi.web.commercant;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Panier;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.CommerceService;
import com.antigaspi.service.PanierService;
import com.antigaspi.service.impl.CommerceServiceImpl;
import com.antigaspi.service.impl.PanierServiceImpl;
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
 * CRUD des paniers du commerçant connecté.
 *  GET  ?action=nouveau          formulaire vide (par défaut)
 *  GET  ?action=modifier&id=X    formulaire prérempli
 *  POST action=enregistrer       crée (sans id) ou modifie (avec id)
 *  POST action=supprimer&id=X    supprime
 * Le commerçant est toujours celui de la session ; la propriété du panier est vérifiée par le service.
 */
@WebServlet("/commercant/paniers")
public class PanierCrudServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PanierCrudServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/commercant/panier-form.jsp";

    private final PanierService panierService = new PanierServiceImpl();
    private final CommerceService commerceService = new CommerceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur commercant = SessionUtil.utilisateurConnecte(req);
        if (commercant == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            Commerce commerce = commerceService.trouverParProprietaire(commercant.getId());
            if ("modifier".equals(req.getParameter("action"))) {
                Panier panier = panierService.trouverParId(lireId(req.getParameter("id")));
                if (!panier.getCommerce().getId().equals(commerce.getId())) {
                    throw new BusinessException("Vous n'êtes pas propriétaire de ce panier");
                }
                afficher(req, resp, PanierFormulaire.depuis(panier), true);
            } else {
                afficher(req, resp, PanierFormulaire.vide(), false);
            }
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/commercant/dashboard");
        } catch (RuntimeException e) {
            erreurTechnique(resp, e);
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
            if ("supprimer".equals(req.getParameter("action"))) {
                supprimer(req, resp, commercant);
            } else {
                enregistrer(req, resp, commercant);
            }
        } catch (RuntimeException e) {
            erreurTechnique(resp, e);
        }
    }

    private void supprimer(HttpServletRequest req, HttpServletResponse resp, Utilisateur commercant)
            throws IOException {
        try {
            panierService.supprimer(lireId(req.getParameter("id")), commercant.getId());
            FlashUtil.succes(req, "Panier supprimé");
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/commercant/dashboard");
    }

    private void enregistrer(HttpServletRequest req, HttpServletResponse resp, Utilisateur commercant)
            throws ServletException, IOException {
        PanierFormulaire formulaire = PanierFormulaire.lire(req);
        boolean edition = !formulaire.getId().isEmpty();

        if (!formulaire.valider()) {
            afficher(req, resp, formulaire, edition); // erreurs par champ
            return;
        }
        try {
            Panier panier = formulaire.getPanier();
            if (edition) {
                panierService.modifier(panier, commercant.getId());
                FlashUtil.succes(req, "Panier modifié");
            } else {
                panierService.creer(panier, commercant.getId());
                FlashUtil.succes(req, "Panier publié : il est maintenant visible par les clients");
            }
            resp.sendRedirect(req.getContextPath() + "/commercant/dashboard");
        } catch (BusinessException e) {
            req.setAttribute(FlashUtil.ERREUR, e.getMessage()); // erreur générale, on garde les valeurs saisies
            afficher(req, resp, formulaire, edition);
        }
    }

    private void afficher(HttpServletRequest req, HttpServletResponse resp, PanierFormulaire formulaire,
                          boolean edition) throws ServletException, IOException {
        req.setAttribute("panier", formulaire);
        req.setAttribute("erreurs", formulaire.getErreurs());
        req.setAttribute("modeEdition", edition);
        req.getRequestDispatcher(VUE).forward(req, resp);
    }

    private void erreurTechnique(HttpServletResponse resp, RuntimeException e) throws IOException {
        LOG.log(Level.SEVERE, "Erreur technique dans la gestion des paniers", e);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private static Long lireId(String valeur) {
        try {
            return valeur == null ? null : Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
