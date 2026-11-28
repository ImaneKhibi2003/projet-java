package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.service.dto.StatsAdmin;
import com.antigaspi.service.dto.StatsCommercant;
import com.antigaspi.service.impl.RetraitServiceImpl;
import com.antigaspi.service.impl.StatsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Données de démo : commerce 1 (commerçant 2) : 2 paniers, 2 réservations dont 1 RETIREE (1 x 30 DH).
 * Commerce 2 (commerçant 3) : 2 paniers, 2 réservations dont 1 RETIREE (2 x 35 DH) et 1 ANNULEE.
 * Commerce 3 (commerçant 4) : 2 paniers, aucune réservation.
 */
class StatsServiceTest {

    private StatsService service;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new StatsServiceImpl();
    }

    private static void assertMontant(String attendu, BigDecimal reel) {
        assertEquals(0, new BigDecimal(attendu).compareTo(reel), "montant attendu " + attendu + " mais " + reel);
    }

    @Test
    void statsDeLaBoulangerie() {
        StatsCommercant s = service.statsCommercant(2L);
        assertEquals(2, s.getNbPaniers());
        assertEquals(2, s.getNbReservations());
        assertEquals(1, s.getInvendusSauves());
        assertMontant("30", s.getChiffreAffaires());
    }

    @Test
    void statsDuRestaurantIgnorentLesReservationsAnnulees() {
        StatsCommercant s = service.statsCommercant(3L);
        assertEquals(2, s.getNbPaniers());
        assertEquals(2, s.getNbReservations());
        assertEquals(2, s.getInvendusSauves());
        assertMontant("70", s.getChiffreAffaires());
    }

    @Test
    void statsDunCommerceSansReservation() {
        StatsCommercant s = service.statsCommercant(4L);
        assertEquals(2, s.getNbPaniers());
        assertEquals(0, s.getNbReservations());
        assertEquals(0, s.getInvendusSauves());
        assertMontant("0", s.getChiffreAffaires());
    }

    @Test
    void statsRefuseUnUtilisateurSansCommerce() {
        assertEquals("Aucun commerce n'est associé à votre compte",
                assertThrows(BusinessException.class, () -> service.statsCommercant(5L)).getMessage());
    }

    @Test
    void lesStatsSuiventLesNouveauxRetraits() {
        // la réservation A1B2C3 (1 x 20 DH) est retirée : le chiffre d'affaires passe de 30 à 50
        new RetraitServiceImpl().validerRetrait(2L, "A1B2C3");

        StatsCommercant s = service.statsCommercant(2L);
        assertEquals(2, s.getInvendusSauves());
        assertMontant("50", s.getChiffreAffaires());
    }

    @Test
    void statsAdminGlobales() {
        StatsAdmin s = service.statsAdmin();
        assertEquals(6, s.getNbUtilisateurs());
        assertEquals(3, s.getNbCommerces());
        assertEquals(6, s.getNbPaniers());
        assertEquals(4, s.getNbReservations());
        assertEquals(2, s.getNbRetraits());
        assertEquals(3, s.getInvendusSauves());
        assertMontant("100", s.getChiffreAffairesTotal());
    }
}
