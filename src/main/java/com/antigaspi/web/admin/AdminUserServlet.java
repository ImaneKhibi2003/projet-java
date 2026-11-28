package com.antigaspi.web.admin;

import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.UtilisateurService;
import com.antigaspi.service.impl.UtilisateurServiceImpl;
import com.antigaspi.web.FlashUtil;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestion des utilisateurs par l'administrateur.
 *  GET  ?action=nouveau | modifier&id=X   formulaires
 *  POST action=creer | modifier | role | supprimer
 * Un administrateur ne peut ni supprimer ni changer le rôle de son propre compte.
 */
@WebServlet("/admin/utilisateurs")
public class AdminUserServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminUserServlet.class.getName());
    private static final String VUE_FORMULAIRE = "/WEB-INF/views/admin/utilisateur-form.jsp";

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("nouveau".equals(action)) {
                Map<String, String> valeurs = new HashMap<>();
                valeurs.put("role", Role.CLIENT.name());
                afficherFormulaire(req, resp, valeurs, null);
            } else if ("modifier".equals(action)) {
                Utilisateur u = utilisateurService.trouverParId(lireId(req.getParameter("id")));
                Map<String, String> valeurs = new HashMap<>();
                valeurs.put("nom", u.getNom());
                valeurs.put("prenom", u.getPrenom());
                valeurs.put("email", u.getEmail());
                afficherFormulaire(req, resp, valeurs, u.getId());
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            }
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (RuntimeException e) {
            erreurTechnique(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilisateur admin = SessionUtil.utilisateurConnecte(req);
        if (admin == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        try {
            switch (action) {
                case "creer" -> creer(req, resp);
                case "modifier" -> modifier(req, resp);
                case "role" -> changerRole(req, resp, admin);
                case "supprimer" -> supprimer(req, resp, admin);
                default -> resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            }
        } catch (RuntimeException e) {
            erreurTechnique(resp, e);
        }
    }

    // ----------------------------------------------------------------- actions

    private void creer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Role role = lireRole(req.getParameter("role"));
            Utilisateur u = new Utilisateur();
            u.setNom(req.getParameter("nom"));
            u.setPrenom(req.getParameter("prenom"));
            u.setEmail(req.getParameter("email"));
            u.setRole(role);
            String motDePasse = req.getParameter("motDePasse");

            if (role == Role.COMMERCANT) {
                Commerce c = new Commerce();
                c.setNom(req.getParameter("commerceNom"));
                c.setAdresse(req.getParameter("commerceAdresse"));
                c.setVille(req.getParameter("commerceVille"));
                c.setTelephone(req.getParameter("commerceTelephone"));
                c.setCategorie(req.getParameter("commerceCategorie"));
                utilisateurService.creerCommercant(u, motDePasse, c);
            } else {
                utilisateurService.creer(u, motDePasse);
            }
            FlashUtil.succes(req, "Utilisateur créé : " + u.getEmail());
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (BusinessException e) {
            req.setAttribute(FlashUtil.ERREUR, e.getMessage());
            afficherFormulaire(req, resp, valeursSaisies(req), null); // le mot de passe n'est jamais renvoyé
        }
    }

    private void modifier(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = lireId(req.getParameter("id"));
        try {
            Utilisateur u = new Utilisateur();
            u.setId(id);
            u.setNom(req.getParameter("nom"));
            u.setPrenom(req.getParameter("prenom"));
            u.setEmail(req.getParameter("email"));
            utilisateurService.modifier(u);
            FlashUtil.succes(req, "Utilisateur modifié");
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } catch (BusinessException e) {
            req.setAttribute(FlashUtil.ERREUR, e.getMessage());
            afficherFormulaire(req, resp, valeursSaisies(req), id);
        }
    }

    private void changerRole(HttpServletRequest req, HttpServletResponse resp, Utilisateur admin) throws IOException {
        try {
            Long id = lireId(req.getParameter("id"));
            if (admin.getId().equals(id)) {
                throw new BusinessException("Vous ne pouvez pas modifier votre propre rôle");
            }
            utilisateurService.changerRole(id, lireRole(req.getParameter("role")));
            FlashUtil.succes(req, "Rôle modifié");
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }

    private void supprimer(HttpServletRequest req, HttpServletResponse resp, Utilisateur admin) throws IOException {
        try {
            Long id = lireId(req.getParameter("id"));
            if (admin.getId().equals(id)) {
                throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte");
            }
            utilisateurService.supprimer(id);
            FlashUtil.succes(req, "Utilisateur supprimé");
        } catch (BusinessException e) {
            FlashUtil.erreur(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }

    // ------------------------------------------------------------------ outils

    private void afficherFormulaire(HttpServletRequest req, HttpServletResponse resp,
                                    Map<String, String> valeurs, Long idEdition) throws ServletException, IOException {
        req.setAttribute("valeurs", valeurs);
        req.setAttribute("modeEdition", idEdition != null);
        req.setAttribute("idEdition", idEdition);
        req.getRequestDispatcher(VUE_FORMULAIRE).forward(req, resp);
    }

    /** Valeurs saisies à réafficher en cas d'erreur (sauf le mot de passe). */
    private Map<String, String> valeursSaisies(HttpServletRequest req) {
        Map<String, String> valeurs = new HashMap<>();
        for (String champ : new String[]{"nom", "prenom", "email", "role", "commerceNom", "commerceAdresse",
                "commerceVille", "commerceTelephone", "commerceCategorie"}) {
            String v = req.getParameter(champ);
            valeurs.put(champ, v == null ? "" : v);
        }
        return valeurs;
    }

    private static Role lireRole(String valeur) {
        try {
            return Role.valueOf(valeur.trim());
        } catch (RuntimeException e) { // null ou valeur inconnue
            throw new BusinessException("Rôle invalide");
        }
    }

    private static Long lireId(String valeur) {
        try {
            return valeur == null ? null : Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void erreurTechnique(HttpServletResponse resp, RuntimeException e) throws IOException {
        LOG.log(Level.SEVERE, "Erreur technique dans la gestion des utilisateurs", e);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
