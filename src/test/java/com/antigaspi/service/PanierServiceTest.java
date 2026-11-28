package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.service.impl.PanierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Suppose que la date du jour est avant le 31/12/2026 (fenêtres de retrait de data.sql). */
class PanierServiceTest {

    private PanierService service;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new PanierServiceImpl();
    }

    @Test
    void rechercherSansFiltreRetourneLes5PaniersDisponibles() {
        assertEquals(5, service.rechercher(null, null, null).size());
    }

    @Test
    void triParDateMetLaFinDeRetraitLaPlusProcheEnPremier() {
        List<Panier> paniers = service.rechercher(null, null, "date");
        assertEquals(5L, paniers.get(0).getId()); // fin à 19h
        for (int i = 1; i < paniers.size(); i++) {
            assertFalse(paniers.get(i).getDateFinRetrait().isBefore(paniers.get(i - 1).getDateFinRetrait()));
        }
    }

    @Test
    void triParPrixEstCroissant() {
        List<Panier> paniers = service.rechercher(null, null, "prix");
        assertEquals(1L, paniers.get(0).getId());               // 20 DH
        assertEquals(3L, paniers.get(paniers.size() - 1).getId()); // 35 DH
        for (int i = 1; i < paniers.size(); i++) {
            assertTrue(paniers.get(i).getPrixReduit().compareTo(paniers.get(i - 1).getPrixReduit()) >= 0);
        }
    }

    @Test
    void rechercherFiltreParCategorieEtParPrix() {
        assertEquals(2, service.rechercher("Boulangerie", null, null).size());
        assertEquals(2, service.rechercher(null, new BigDecimal("25"), null).size());
        assertEquals(1, service.rechercher("Restaurant", new BigDecimal("30"), "prix").size());
    }

    @Test
    void trouverParIdRetourneLePanierAvecSonCommerce() {
        Panier p = service.trouverParId(1L);
        assertEquals("Panier viennoiseries", p.getTitre());
        assertEquals("Boulangerie Al Baraka", p.getCommerce().getNom());
    }

    @Test
    void trouverParIdInconnuOuNulLeveUneErreurMetier() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.trouverParId(9999L));
        assertEquals("Panier introuvable", e.getMessage());
        assertThrows(BusinessException.class, () -> service.trouverParId(null));
    }

    @Test
    void listerCategoriesEtListerParCommerce() {
        assertEquals(3, service.listerCategories().size());
        assertEquals(2, service.listerParCommerce(2L).size());
    }
}
