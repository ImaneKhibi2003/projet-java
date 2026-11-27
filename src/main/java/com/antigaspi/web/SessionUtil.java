package com.antigaspi.web;

import com.antigaspi.model.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Accès à l'utilisateur connecté (attribut de session "utilisateur", voir contrat §8). */
public final class SessionUtil {

    public static final String ATTR_UTILISATEUR = "utilisateur";
    public static final String ATTR_URL_DEMANDEE = "urlDemandee";
    private static final int DUREE_INACTIVITE_SECONDES = 30 * 60;

    private SessionUtil() {
    }

    /** L'utilisateur connecté, ou null. */
    public static Utilisateur utilisateurConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Utilisateur) session.getAttribute(ATTR_UTILISATEUR);
    }

    /** Ouvre la session de l'utilisateur (nouvel identifiant de session : protection contre la fixation). */
    public static void connecter(HttpServletRequest request, Utilisateur utilisateur) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR_UTILISATEUR, utilisateur);
        session.setMaxInactiveInterval(DUREE_INACTIVITE_SECONDES);
    }

    /** Retourne puis oublie l'URL que l'utilisateur voulait ouvrir avant d'être renvoyé vers /login. */
    public static String retirerUrlDemandee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        String url = (String) session.getAttribute(ATTR_URL_DEMANDEE);
        session.removeAttribute(ATTR_URL_DEMANDEE);
        return url;
    }
}
