package com.antigaspi.filter;

import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.AccesUtil;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Contrôle d'accès par rôle sur /client/*, /commercant/* et /admin/*.
 * VERSION PROVISOIRE écrite par A (A4.1) ; la personne B peut la reprendre (B3.10).
 * - non connecté  -> redirection vers /login (l'URL demandée est mémorisée)
 * - mauvais rôle  -> erreur 403
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String chemin = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        Role roleRequis = AccesUtil.roleRequis(chemin);

        if (roleRequis == null) {
            chain.doFilter(request, response); // page publique
            return;
        }

        Utilisateur utilisateur = SessionUtil.utilisateurConnecte(req);
        if (utilisateur == null) {
            if ("GET".equals(req.getMethod())) {
                String url = chemin + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
                req.getSession().setAttribute(SessionUtil.ATTR_URL_DEMANDEE, url);
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (utilisateur.getRole() != roleRequis) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}
