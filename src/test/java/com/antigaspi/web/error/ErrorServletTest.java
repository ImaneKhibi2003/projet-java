package com.antigaspi.web.error;

import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.web.SessionUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorServletTest {

    private ErrorServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void preparer() {
        servlet = new ErrorServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        when(req.getContextPath()).thenReturn("/antigaspi");
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(req.getAttribute("jakarta.servlet.error.request_uri")).thenReturn("/antigaspi/test");
    }

    private void erreur(int code) {
        when(req.getAttribute("jakarta.servlet.error.status_code")).thenReturn(code);
    }

    private void connecte(Role role) {
        Utilisateur u = new Utilisateur();
        u.setId(1L);
        u.setEmail("test@antigaspi.ma");
        u.setRole(role);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(SessionUtil.ATTR_UTILISATEUR)).thenReturn(u);
        when(req.getSession(false)).thenReturn(session);
    }

    @Test
    void erreur404AfficheLaPageIntrouvablePourUnVisiteur() throws Exception {
        erreur(404);

        servlet.service(req, resp);

        verify(resp).setStatus(404);
        verify(req).setAttribute("codeErreur", 404);
        verify(req).setAttribute("titreErreur", "Page introuvable");
        verify(req).setAttribute("referenceErreur", null);          // pas de référence pour une erreur 4xx
        verify(req).setAttribute("lienRetour", "/antigaspi/paniers");
        verify(req).setAttribute("libelleRetour", "Voir les paniers");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void erreur403ProposeLeRetourVersLEspaceDeLUtilisateur() throws Exception {
        erreur(403);
        connecte(Role.COMMERCANT);

        servlet.service(req, resp);

        verify(resp).setStatus(403);
        verify(req).setAttribute("titreErreur", "Accès interdit");
        verify(req).setAttribute("lienRetour", "/antigaspi/commercant/dashboard");
        verify(req).setAttribute("libelleRetour", "Retour à mon espace");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void erreur500GenereUneReferenceEtNAfficheAucuneExceptionTechnique() throws Exception {
        erreur(500);
        when(req.getAttribute("jakarta.servlet.error.exception")).thenReturn(new IllegalStateException("détail secret"));

        servlet.service(req, resp);

        verify(resp).setStatus(500);
        verify(req).setAttribute(eq("referenceErreur"), notNull());
        verify(req).setAttribute("messageErreur",
                "Une erreur technique est survenue. Elle a été enregistrée : réessayez dans un instant.");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void accesDirectALaPageDErreurRedirigeVersLAccueil() throws Exception {
        // aucun attribut d'erreur : /erreur ouvert à la main
        servlet.service(req, resp);

        verify(resp).sendRedirect("/antigaspi/");
        verify(dispatcher, never()).forward(req, resp);
    }

    @Test
    void lesTitresEtMessagesCouvrentLesCodesGeres() {
        assertEquals("Requête invalide", ErrorServlet.titre(400));
        assertEquals("Accès interdit", ErrorServlet.titre(403));
        assertEquals("Page introuvable", ErrorServlet.titre(404));
        assertEquals("Action non autorisée", ErrorServlet.titre(405));
        assertEquals("Erreur interne", ErrorServlet.titre(500));
        assertEquals("Une erreur est survenue", ErrorServlet.titre(418));
        assertEquals("Vous n'avez pas les droits nécessaires pour accéder à cette page.", ErrorServlet.message(403));
        assertEquals("Une erreur inattendue est survenue.", ErrorServlet.message(418));
    }
}
