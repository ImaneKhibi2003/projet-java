package com.antigaspi.filter;

import com.antigaspi.web.FlashUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Déplace les messages flash de la session vers la requête (pour une seule page). */
@WebFilter("/*")
public class FlashFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!estRessourceStatique(req)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                deplacer(session, req, FlashUtil.SUCCES);
                deplacer(session, req, FlashUtil.ERREUR);
            }
        }
        chain.doFilter(request, response);
    }

    private void deplacer(HttpSession session, HttpServletRequest req, String cle) {
        Object message = session.getAttribute(cle);
        if (message != null) {
            req.setAttribute(cle, message);
            session.removeAttribute(cle);
        }
    }

    // Les feuilles de style, images... ne doivent pas "consommer" le message avant la page
    private boolean estRessourceStatique(HttpServletRequest req) {
        String uri = req.getRequestURI().toLowerCase();
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif")
                || uri.endsWith(".svg") || uri.endsWith(".ico") || uri.endsWith(".woff")
                || uri.endsWith(".woff2") || uri.endsWith(".map");
    }
}
