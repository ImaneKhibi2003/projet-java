package com.antigaspi.dao;

import com.antigaspi.model.Panier;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Vérifie les annotations Bean Validation des entités (sans base de données). */
class ValidationEntitesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void utilisateurValideNeProduitAucuneViolation() {
        Utilisateur u = new Utilisateur();
        u.setNom("Alaoui");
        u.setPrenom("Sara");
        u.setEmail("sara@test.ma");
        u.setMotDePasseHash("hash");
        u.setRole(Role.CLIENT);
        assertTrue(validator.validate(u).isEmpty());
    }

    @Test
    void emailInvalideEtNomVideSontRefuses() {
        Utilisateur u = new Utilisateur();
        u.setNom(" ");
        u.setPrenom("Sara");
        u.setEmail("pas-un-email");
        u.setMotDePasseHash("hash");
        u.setRole(Role.CLIENT);
        assertEquals(2, validator.validate(u).size());
    }

    @Test
    void prixReduitSuperieurAuPrixInitialEstRefuse() {
        Panier p = new Panier();
        p.setTitre("Test");
        p.setCategorie("Test");
        p.setPrixInitial(new BigDecimal("10.00"));
        p.setPrixReduit(new BigDecimal("20.00"));
        p.setQuantiteTotale(2);
        p.setQuantiteRestante(2);
        p.setDateDebutRetrait(LocalDateTime.now());
        p.setDateFinRetrait(LocalDateTime.now().plusHours(2));
        assertTrue(validator.validateProperty(p, "prixCoherent").size() == 1);
    }

    @Test
    void dateFinAvantDateDebutEstRefusee() {
        Panier p = new Panier();
        p.setDateDebutRetrait(LocalDateTime.now());
        p.setDateFinRetrait(LocalDateTime.now().minusHours(1));
        assertEquals(1, validator.validateProperty(p, "datesCoherentes").size());
    }

    @Test
    void quantiteNulleEstRefusee() {
        Panier p = new Panier();
        p.setQuantiteTotale(0);
        assertFalse(validator.validateProperty(p, "quantiteTotale").isEmpty());
    }
}
