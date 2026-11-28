package com.antigaspi.web.error;

import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.AccesUtil;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée unique des erreurs (voir web.xml : codes 400, 403, 404, 405, 500 et exceptions non rattrapées).
 * - journalise l'erreur avec java.util.logging (jamais de détail technique affiché à l'utilisateur) ;
 * - affiche WEB-INF/views/error/error.jsp avec un message adapté et un lien de retour selon le rôle.
 *
 * Le conteneur rejoue la requête d'origine avec sa méthode HTTP (un POST en erreur arrive ici en POST) :
 * c'est pourquoi service() est redéfini au lieu de doGet()/doPost().
 */
@WebServlet("/erreur")
public class ErrorServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ErrorServlet.class.getName());
    private static final String VUE = "/WEB-INF/views/error/error.jsp";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer code = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
        Throwable exception = (Throwable) req.getAttribute("jakarta.servlet.error.exception");
        String uri = (String) req.getAttribute("jakarta.servlet.error.request_uri");

        if (code == null) { // quelqu'un a ouvert /erreur directement
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Utilisateur utilisateur = SessionUtil.utilisateurConnecte(req);
        String reference = code >= 500 ? genererReference() : null;
        journaliser(code, exception, uri, utilisateur, reference);

        req.setAttribute("codeErreur", code);
        req.setAttribute("titreErreur", titre(code));
        req.setAttribute("messageErreur", message(code));
        req.setAttribute("referenceErreur", reference);
        if (utilisateur != null) {
            req.setAttribute("lienRetour", req.getContextPath() + AccesUtil.accueil(utilisateur.getRole()));
            req.setAttribute("libelleRetour", "Retour à mon espace");
        } else {
            req.setAttribute("lienRetour", req.getContextPath() + "/paniers");
            req.setAttribute("libelleRetour", "Voir les paniers");
        }

        resp.setStatus(code);
        req.getRequestDispatcher(VUE).forward(req, resp);
    }

    private static void journaliser(int code, Throwable exception, String uri, Utilisateur utilisateur,
                                    String reference) {
        String qui = utilisateur == null ? "visiteur non connecté" : utilisateur.getEmail();
        String message = "Erreur " + code + " sur " + uri + " (" + qui + ")"
                + (reference == null ? "" : " [réf. " + reference + "]");
        if (exception != null || code >= 500) {
            LOG.log(Level.SEVERE, message, exception);
        } else if (code == 404) {
            LOG.log(Level.INFO, message);
        } else {
            LOG.log(Level.WARNING, message);
        }
    }

    static String titre(int code) {
        return switch (code) {
            case 400 -> "Requête invalide";
            case 403 -> "Accès interdit";
            case 404 -> "Page introuvable";
            case 405 -> "Action non autorisée";
            case 500 -> "Erreur interne";
            default -> "Une erreur est survenue";
        };
    }

    static String message(int code) {
        return switch (code) {
            case 400 -> "La requête envoyée est incorrecte. Vérifiez les informations saisies et réessayez.";
            case 403 -> "Vous n'avez pas les droits nécessaires pour accéder à cette page.";
            case 404 -> "La page demandée n'existe pas ou a été déplacée.";
            case 405 -> "Cette action n'est pas autorisée sur cette page.";
            case 500 -> "Une erreur technique est survenue. Elle a été enregistrée : réessayez dans un instant.";
            default -> "Une erreur inattendue est survenue.";
        };
    }

    /** Référence courte à communiquer au support : elle figure aussi dans le journal du serveur. */
    private static String genererReference() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
