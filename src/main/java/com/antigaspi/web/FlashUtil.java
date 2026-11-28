package com.antigaspi.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Messages flash : un message posé avant une redirection est affiché une seule fois
 * sur la page suivante (voir FlashFilter et layout/messages.jsp).
 */
public final class FlashUtil {

    public static final String SUCCES = "flashSucces";
    public static final String ERREUR = "flashErreur";

    private FlashUtil() {
    }

    public static void succes(HttpServletRequest request, String message) {
        request.getSession().setAttribute(SUCCES, message);
    }

    public static void erreur(HttpServletRequest request, String message) {
        request.getSession().setAttribute(ERREUR, message);
    }
}
