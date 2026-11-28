package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.model.StatutPanier;
import com.antigaspi.service.impl.PanierServiceImpl;
import com.antigaspi.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Commerçants (data.sql) : 2 = boulangerie (paniers 1, 2), 3 = restaurant (paniers 3, 4), 4 = primeur (paniers 5, 6).
 * Panier 1 : total 5, restant 4 (1 réservé). Panier 3 : total 4, restant 2 (2 retirés).
 * Panier 4 : total 3, restant 3. Panier 6 : expiré, total 2, restant 2.
 */
class PanierServiceCrudTest {

    private PanierService service;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new PanierServiceImpl();
    }

    private Panier donnees(Long id, int quantiteTotale) {
        Panier p = new Panier();
        p.setId(id);
        p.setTitre("Panier de test");
        p.setDescription("Description");
        p.setCategorie("Boulangerie");
        p.setPrixInitial(new BigDecimal("40.00"));
        p.setPrixReduit(new BigDecimal("12.00"));
        p.setQuantiteTotale(quantiteTotale);
        p.setDateDebutRetrait(LocalDateTime.now());
        p.setDateFinRetrait(LocalDateTime.now().plusDays(2));
        return p;
    }

    private String erreurCreer(Panier p, long commercant) {
        return assertThrows(BusinessException.class, () -> service.creer(p, commercant)).getMessage();
    }

    private String erreurModifier(Panier p, long commercant) {
        return assertThrows(BusinessException.class, () -> service.modifier(p, commercant)).getMessage();
    }

    private String erreurSupprimer(long panierId, long commercant) {
        return assertThrows(BusinessException.class, () -> service.supprimer(panierId, commercant)).getMessage();
    }

    // ------------------------------------------------------------------ creer

    @Test
    void creerCasNominal() {
        Panier cree = service.creer(donnees(null, 5), 2L);

        assertNotNull(cree.getId());
        Panier relu = service.trouverParId(cree.getId());
        assertEquals(5, relu.getQuantiteRestante());
        assertEquals(StatutPanier.DISPONIBLE, relu.getStatut());
        assertEquals("Boulangerie Al Baraka", relu.getCommerce().getNom());
        assertEquals(6, service.rechercher(null, null, null).size()); // 5 de démo + le nouveau
    }

    @Test
    void creerIgnoreUnIdentifiantEtUnStockFournisParLeFormulaire() {
        Panier p = donnees(999L, 3);
        p.setQuantiteRestante(1);
        p.setStatut(StatutPanier.EXPIRE);

        Panier cree = service.creer(p, 2L);

        assertNotEquals(999L, cree.getId());
        Panier relu = service.trouverParId(cree.getId());
        assertEquals(3, relu.getQuantiteRestante());
        assertEquals(StatutPanier.DISPONIBLE, relu.getStatut());
    }

    @Test
    void creerRefuseUnUtilisateurSansCommerce() {
        assertEquals("Aucun commerce n'est associé à votre compte", erreurCreer(donnees(null, 5), 5L)); // client
    }

    @Test
    void creerRefuseUneDateDeFinPassee() {
        Panier p = donnees(null, 5);
        p.setDateDebutRetrait(LocalDateTime.now().minusHours(5));
        p.setDateFinRetrait(LocalDateTime.now().minusHours(1));
        assertEquals("La date de fin doit être dans le futur", erreurCreer(p, 2L));
    }

    @Test
    void creerRefuseUneDateDeFinAvantLeDebut() {
        Panier p = donnees(null, 5);
        p.setDateDebutRetrait(LocalDateTime.now().plusDays(1));
        p.setDateFinRetrait(LocalDateTime.now().plusHours(2));
        assertEquals("La date de fin doit être après la date de début", erreurCreer(p, 2L));
    }

    @Test
    void creerRefuseUnPrixReduitSuperieurAuPrixInitial() {
        Panier p = donnees(null, 5);
        p.setPrixReduit(new BigDecimal("50.00"));
        assertEquals("Le prix réduit doit être inférieur ou égal au prix initial", erreurCreer(p, 2L));
    }

    @Test
    void creerRefuseUneQuantiteNulle() {
        assertEquals("La quantité doit être au moins 1", erreurCreer(donnees(null, 0), 2L));
    }

    @Test
    void creerRefuseUnTitreVide() {
        Panier p = donnees(null, 5);
        p.setTitre("  ");
        assertEquals("Le titre est obligatoire", erreurCreer(p, 2L));
    }

    // --------------------------------------------------------------- modifier

    @Test
    void modifierMetAJourLesChampsEtRecalculeLeStock() {
        // panier 1 : 1 déjà réservé ; on passe le total de 5 à 8 -> il en reste 7
        Panier modifie = service.modifier(donnees(1L, 8), 2L);

        assertEquals("Panier de test", modifie.getTitre());
        Panier relu = service.trouverParId(1L);
        assertEquals(8, relu.getQuantiteTotale());
        assertEquals(7, relu.getQuantiteRestante());
        assertEquals(StatutPanier.DISPONIBLE, relu.getStatut());
        assertEquals(0, new BigDecimal("12.00").compareTo(relu.getPrixReduit()));
    }

    @Test
    void modifierRefuseUneQuantiteInferieureAuxPaniersDejaReserves() {
        // panier 3 : 2 retirés ; on ne peut pas descendre le total à 1
        assertEquals("La quantité totale ne peut pas être inférieure aux paniers déjà réservés (2)",
                erreurModifier(donnees(3L, 1), 3L));
        assertEquals(2, service.trouverParId(3L).getQuantiteRestante()); // inchangé
    }

    @Test
    void modifierRefuseUnCommercantQuiNEstPasProprietaire() {
        assertEquals("Vous n'êtes pas propriétaire de ce panier", erreurModifier(donnees(1L, 5), 3L));
        assertEquals("Boulangerie Al Baraka", service.trouverParId(1L).getCommerce().getNom());
        assertEquals("Panier viennoiseries", service.trouverParId(1L).getTitre()); // inchangé
    }

    @Test
    void modifierRepasseUnPanierEpuiseADisponibleQuandOnAugmenteLeStock() {
        new ReservationServiceImpl().reserver(5L, 4L, 3); // Sara prend tout le panier 4
        assertEquals(StatutPanier.EPUISE, service.trouverParId(4L).getStatut());

        service.modifier(donnees(4L, 5), 3L); // le restaurateur passe le total de 3 à 5

        Panier relu = service.trouverParId(4L);
        assertEquals(2, relu.getQuantiteRestante());
        assertEquals(StatutPanier.DISPONIBLE, relu.getStatut());
    }

    @Test
    void modifierRemetEnVenteUnPanierExpireAvecDeNouvellesDates() {
        assertEquals(StatutPanier.EXPIRE, service.trouverParId(6L).getStatut());

        service.modifier(donnees(6L, 2), 4L);

        assertEquals(StatutPanier.DISPONIBLE, service.trouverParId(6L).getStatut());
    }

    @Test
    void modifierRefuseUnPanierInconnuOuSansIdentifiant() {
        assertEquals("Panier introuvable", erreurModifier(donnees(9999L, 5), 2L));
        assertEquals("Panier introuvable", erreurModifier(donnees(null, 5), 2L));
    }

    // -------------------------------------------------------------- supprimer

    @Test
    void supprimerUnPanierSansReservation() {
        Panier cree = service.creer(donnees(null, 5), 2L);

        service.supprimer(cree.getId(), 2L);

        assertEquals("Panier introuvable",
                assertThrows(BusinessException.class, () -> service.trouverParId(cree.getId())).getMessage());
    }

    @Test
    void supprimerRefuseUnPanierDejaReserve() {
        // le panier 1 a la réservation A1B2C3
        assertEquals("Ce panier a déjà des réservations : il ne peut plus être supprimé", erreurSupprimer(1L, 2L));
        assertNotNull(service.trouverParId(1L));
    }

    @Test
    void supprimerRefuseUnCommercantQuiNEstPasProprietaire() {
        assertEquals("Vous n'êtes pas propriétaire de ce panier", erreurSupprimer(5L, 2L));
        assertNotNull(service.trouverParId(5L));
    }

    @Test
    void supprimerRefuseUnPanierInconnu() {
        assertEquals("Panier introuvable", erreurSupprimer(9999L, 2L));
    }
}
