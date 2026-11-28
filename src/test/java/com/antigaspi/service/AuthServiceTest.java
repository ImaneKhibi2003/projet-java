package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.PasswordUtil;
import com.antigaspi.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService auth;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        auth = new AuthServiceImpl();
    }

    @Test
    void loginReussiAvecLesComptesDeDemo() {
        Utilisateur admin = auth.login("admin@antigaspi.ma", "password123");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(Role.CLIENT, auth.login("sara@antigaspi.ma", "password123").getRole());
        assertEquals(Role.COMMERCANT, auth.login("boulangerie@antigaspi.ma", "password123").getRole());
    }

    @Test
    void loginIgnoreLaCasseEtLesEspacesDeLEmail() {
        assertNotNull(auth.login("  ADMIN@Antigaspi.MA ", "password123"));
    }

    @Test
    void loginRefuseUnMauvaisMotDePasse() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> auth.login("admin@antigaspi.ma", "mauvais"));
        assertEquals("Email ou mot de passe incorrect", e.getMessage());
    }

    @Test
    void loginRefuseUnEmailInconnuAvecLeMemeMessage() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> auth.login("inconnu@test.ma", "password123"));
        assertEquals("Email ou mot de passe incorrect", e.getMessage());
    }

    @Test
    void loginRefuseLesChampsVides() {
        assertThrows(BusinessException.class, () -> auth.login("", ""));
        assertThrows(BusinessException.class, () -> auth.login(null, null));
    }

    @Test
    void registerCreeUnClientAvecUnMotDePasseHache() {
        Utilisateur u = auth.register("Nom", "Prenom", "Nouveau@Test.ma", "motdepasse1");

        assertNotNull(u.getId());
        assertEquals(Role.CLIENT, u.getRole());
        assertEquals("nouveau@test.ma", u.getEmail());
        assertNotEquals("motdepasse1", u.getMotDePasseHash());
        assertTrue(PasswordUtil.verify("motdepasse1", u.getMotDePasseHash()));

        // le nouveau compte peut se connecter
        assertEquals(u.getId(), auth.login("nouveau@test.ma", "motdepasse1").getId());
    }

    @Test
    void registerRefuseUnEmailDejaUtilise() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> auth.register("Nom", "Prenom", "SARA@antigaspi.ma", "motdepasse1"));
        assertEquals("Cet email est déjà utilisé", e.getMessage());
    }

    @Test
    void registerRefuseUnMotDePasseTropCourt() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> auth.register("Nom", "Prenom", "court@test.ma", "abc"));
        assertEquals("Le mot de passe doit contenir au moins 8 caractères", e.getMessage());
    }

    @Test
    void registerRefuseUnEmailInvalideOuUnNomVide() {
        assertThrows(BusinessException.class,
                () -> auth.register("Nom", "Prenom", "pas-un-email", "motdepasse1"));
        assertThrows(BusinessException.class,
                () -> auth.register(" ", "Prenom", "ok@test.ma", "motdepasse1"));
    }

    @Test
    void passwordUtilGenereUnHashDifferentACHaqueFois() {
        String h1 = PasswordUtil.hash("password123");
        String h2 = PasswordUtil.hash("password123");
        assertNotEquals(h1, h2);
        assertTrue(PasswordUtil.verify("password123", h1));
        assertFalse(PasswordUtil.verify("autre", h1));
        assertFalse(PasswordUtil.verify("password123", "pas-un-hash"));
    }
}
