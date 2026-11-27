package com.antigaspi.dao;

import com.antigaspi.model.Commerce;
import com.antigaspi.model.Panier;
import com.antigaspi.model.StatutPanier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ces tests supposent que la date du jour est avant le 31/12/2026
 * (fenêtres de retrait des paniers de démonstration).
 */
class PanierDaoTest extends AbstractDaoTest {

    private Panier nouveauPanier() {
        Commerce commerce = commerceDao.findById(1L).orElseThrow();
        Panier p = new Panier();
        p.setCommerce(commerce);
        p.setTitre("Panier test");
        p.setCategorie("Boulangerie");
        p.setPrixInitial(new BigDecimal("50.00"));
        p.setPrixReduit(new BigDecimal("15.00"));
        p.setQuantiteTotale(4);
        p.setQuantiteRestante(4);
        p.setDateDebutRetrait(LocalDateTime.now());
        p.setDateFinRetrait(LocalDateTime.now().plusDays(2));
        return p;
    }

    @Test
    void findAllRetourneLes6Paniers() {
        assertEquals(6, panierDao.findAll().size());
    }

    @Test
    void findByIdChargeLeCommerce() {
        Panier p = panierDao.findById(1L).orElseThrow();
        assertEquals("Panier viennoiseries", p.getTitre());
        assertEquals("Boulangerie Al Baraka", p.getCommerce().getNom());
        assertEquals(StatutPanier.DISPONIBLE, p.getStatut());
    }

    @Test
    void saveCreeUnPanierDisponibleParDefaut() {
        Panier cree = panierDao.save(nouveauPanier());
        assertNotNull(cree.getId());
        assertEquals(StatutPanier.DISPONIBLE, panierDao.findById(cree.getId()).orElseThrow().getStatut());
    }

    @Test
    void saveModifieUnPanier() {
        Panier p = panierDao.findById(1L).orElseThrow();
        p.setTitre("Titre modifié");
        panierDao.save(p);
        assertEquals("Titre modifié", panierDao.findById(1L).orElseThrow().getTitre());
    }

    @Test
    void deleteByIdSupprimeUnPanier() {
        Panier cree = panierDao.save(nouveauPanier());
        panierDao.deleteById(cree.getId());
        assertTrue(panierDao.findById(cree.getId()).isEmpty());
    }

    @Test
    void findDisponiblesSansFiltreExclutLePanierExpire() {
        List<Panier> paniers = panierDao.findDisponibles(null, null);
        assertEquals(5, paniers.size());
        assertTrue(paniers.stream().noneMatch(p -> p.getId() == 6L));
    }

    @Test
    void findDisponiblesFiltreParCategorie() {
        List<Panier> paniers = panierDao.findDisponibles("Boulangerie", null);
        assertEquals(2, paniers.size());
        assertTrue(paniers.stream().allMatch(p -> p.getCategorie().equals("Boulangerie")));
    }

    @Test
    void findDisponiblesFiltreParPrixMax() {
        // prix réduits : 20, 30, 35, 30, 25 -> seuls 20 et 25 sont <= 25
        List<Panier> paniers = panierDao.findDisponibles(null, new BigDecimal("25.00"));
        assertEquals(2, paniers.size());
    }

    @Test
    void findDisponiblesCombineCategorieEtPrix() {
        List<Panier> paniers = panierDao.findDisponibles("Restaurant", new BigDecimal("30.00"));
        assertEquals(1, paniers.size());
        assertEquals(4L, paniers.get(0).getId());
    }

    @Test
    void findDisponiblesIgnoreUneCategorieVide() {
        assertEquals(5, panierDao.findDisponibles("  ", null).size());
    }

    @Test
    void findDisponiblesExclutUnPanierEpuise() {
        Panier p = panierDao.findById(1L).orElseThrow();
        p.setQuantiteRestante(0);
        p.setStatut(StatutPanier.EPUISE);
        panierDao.save(p);

        assertEquals(4, panierDao.findDisponibles(null, null).size());
    }

    @Test
    void findByCommerce() {
        assertEquals(2, panierDao.findByCommerce(1L).size());
        assertEquals(2, panierDao.findByCommerce(3L).size()); // y compris le panier expiré
    }

    @Test
    void findCategoriesRetourneLesCategoriesDistinctes() {
        List<String> categories = panierDao.findCategories();
        assertEquals(3, categories.size());
        assertTrue(categories.containsAll(List.of("Boulangerie", "Restaurant", "Fruits et légumes")));
    }

    @Test
    void findByIdForUpdateDansUneTransaction() {
        Integer restante = JpaUtil.inTransaction(() -> {
            Panier p = panierDao.findByIdForUpdate(1L).orElseThrow();
            p.setQuantiteRestante(p.getQuantiteRestante() - 1);
            return p.getQuantiteRestante();
        });

        assertEquals(3, restante);
        // la modification a été validée à la fin de la transaction
        assertEquals(3, panierDao.findById(1L).orElseThrow().getQuantiteRestante());
    }

    @Test
    void findByIdForUpdateInconnuRetourneVide() {
        assertTrue(panierDao.findByIdForUpdate(9999L).isEmpty());
    }
}
