package com.antigaspi.dao;

import com.antigaspi.model.Reservation;
import com.antigaspi.model.Retrait;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetraitDaoTest extends AbstractDaoTest {

    private Retrait nouveauRetrait() {
        Reservation r = reservationDao.findById(1L).orElseThrow();
        Retrait retrait = new Retrait();
        retrait.setReservation(r);
        return retrait;
    }

    @Test
    void findAllRetourneLes2Retraits() {
        assertEquals(2, retraitDao.findAll().size());
    }

    @Test
    void findByIdChargeLaReservationEtLeCommerce() {
        Retrait r = retraitDao.findById(1L).orElseThrow();
        assertEquals(2L, r.getReservation().getId());
        assertEquals("Boulangerie Al Baraka", r.getReservation().getPanier().getCommerce().getNom());
    }

    @Test
    void saveCreeUnRetraitAvecDate() {
        Retrait cree = retraitDao.save(nouveauRetrait());
        assertNotNull(cree.getId());
        assertNotNull(retraitDao.findById(cree.getId()).orElseThrow().getDateRetrait());
        assertEquals(3, retraitDao.findAll().size());
    }

    @Test
    void deleteByIdSupprimeUnRetrait() {
        retraitDao.deleteById(1L);
        assertTrue(retraitDao.findById(1L).isEmpty());
        assertEquals(1, retraitDao.findAll().size());
    }

    @Test
    void findByReservation() {
        assertTrue(retraitDao.findByReservation(2L).isPresent());
        assertTrue(retraitDao.findByReservation(1L).isEmpty()); // RESERVEE : pas encore retirée
    }

    @Test
    void findByCommerceAndDate() {
        LocalDate jour = LocalDate.of(2026, 11, 23);
        List<?> commerce1 = retraitDao.findByCommerceAndDate(1L, jour);
        List<?> commerce2 = retraitDao.findByCommerceAndDate(2L, jour);

        assertEquals(1, commerce1.size());
        assertEquals(1, commerce2.size());
        assertTrue(retraitDao.findByCommerceAndDate(1L, LocalDate.of(2026, 11, 24)).isEmpty());
        assertTrue(retraitDao.findByCommerceAndDate(3L, jour).isEmpty());
    }
}
