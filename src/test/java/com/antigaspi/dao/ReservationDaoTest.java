package com.antigaspi.dao;

import com.antigaspi.model.Panier;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.model.Utilisateur;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationDaoTest extends AbstractDaoTest {

    private Reservation nouvelleReservation(long panierId, long clientId, String code) {
        Panier panier = panierDao.findById(panierId).orElseThrow();
        Utilisateur client = utilisateurDao.findById(clientId).orElseThrow();
        Reservation r = new Reservation();
        r.setPanier(panier);
        r.setUtilisateur(client);
        r.setQuantite(1);
        r.setCodeRetrait(code);
        return r;
    }

    @Test
    void findAllRetourneLes4Reservations() {
        assertEquals(4, reservationDao.findAll().size());
    }

    @Test
    void findByIdChargePanierCommerceEtClient() {
        Reservation r = reservationDao.findById(1L).orElseThrow();
        assertEquals("A1B2C3", r.getCodeRetrait());
        assertEquals(StatutReservation.RESERVEE, r.getStatut());
        assertEquals("Panier viennoiseries", r.getPanier().getTitre());
        assertEquals("Boulangerie Al Baraka", r.getPanier().getCommerce().getNom());
        assertEquals("sara@antigaspi.ma", r.getUtilisateur().getEmail());
    }

    @Test
    void saveCreeUneReservationRESERVEEAvecDate() {
        Reservation cree = reservationDao.save(nouvelleReservation(5L, 5L, "TEST12"));

        assertNotNull(cree.getId());
        Reservation relue = reservationDao.findById(cree.getId()).orElseThrow();
        assertEquals(StatutReservation.RESERVEE, relue.getStatut());
        assertNotNull(relue.getDateReservation());
    }

    @Test
    void saveModifieLeStatut() {
        Reservation r = reservationDao.findById(1L).orElseThrow();
        r.setStatut(StatutReservation.ANNULEE);
        reservationDao.save(r);
        assertEquals(StatutReservation.ANNULEE, reservationDao.findById(1L).orElseThrow().getStatut());
    }

    @Test
    void deleteByIdSupprimeUneReservation() {
        Reservation cree = reservationDao.save(nouvelleReservation(5L, 6L, "DEL123"));
        reservationDao.deleteById(cree.getId());
        assertTrue(reservationDao.findById(cree.getId()).isEmpty());
    }

    @Test
    void findByUtilisateurTriePlusRecenteEnPremier() {
        List<Reservation> resas = reservationDao.findByUtilisateur(5L);
        assertEquals(2, resas.size());
        assertEquals(1L, resas.get(0).getId()); // 24/11 avant 23/11
        assertEquals(2, reservationDao.findByUtilisateur(6L).size());
        assertTrue(reservationDao.findByUtilisateur(1L).isEmpty());
    }

    @Test
    void findByCommerce() {
        assertEquals(2, reservationDao.findByCommerce(1L).size());
        assertEquals(2, reservationDao.findByCommerce(2L).size());
        assertTrue(reservationDao.findByCommerce(3L).isEmpty());
    }

    @Test
    void findByCodeRetrait() {
        assertEquals(1L, reservationDao.findByCodeRetrait("A1B2C3").orElseThrow().getId());
        assertTrue(reservationDao.findByCodeRetrait("ZZZZZZ").isEmpty());
    }

    @Test
    void existsByCodeRetrait() {
        assertTrue(reservationDao.existsByCodeRetrait("D4E5F6"));
        assertFalse(reservationDao.existsByCodeRetrait("ZZZZZZ"));
    }

    @Test
    void existsActiveByUtilisateurAndPanierNeCompteQueLesReservationsEnCours() {
        assertTrue(reservationDao.existsActiveByUtilisateurAndPanier(5L, 1L));   // RESERVEE
        assertFalse(reservationDao.existsActiveByUtilisateurAndPanier(5L, 2L));  // RETIREE
        assertFalse(reservationDao.existsActiveByUtilisateurAndPanier(6L, 4L));  // ANNULEE
        assertFalse(reservationDao.existsActiveByUtilisateurAndPanier(5L, 5L));  // aucune
    }

    @Test
    void findExpireesEstVideSiAucuneFenetreDepassee() {
        assertTrue(reservationDao.findExpirees().isEmpty());
    }

    @Test
    void findExpireesTrouveUneReservationEnCoursSurUnPanierExpire() {
        // le panier 6 a une fenêtre de retrait dépassée (10-11 novembre)
        reservationDao.save(nouvelleReservation(6L, 5L, "EXP123"));

        List<Reservation> expirees = reservationDao.findExpirees();

        assertEquals(1, expirees.size());
        assertEquals("EXP123", expirees.get(0).getCodeRetrait());
    }
}
