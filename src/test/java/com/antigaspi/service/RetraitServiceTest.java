package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.RetraitDao;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.RetraitDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.Retrait;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.service.impl.ReservationServiceImpl;
import com.antigaspi.service.impl.RetraitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jeu de données (data.sql) : commerçants 2 (boulangerie, commerce 1), 3 (restaurant, commerce 2),
 * 4 (primeur, commerce 3) ; clients Sara = 5 et Karim = 6.
 * Réservations : A1B2C3 RESERVEE (commerce 1), D4E5F6 RETIREE (commerce 1),
 * G7H8J9 RETIREE (commerce 2), K2L3M4 ANNULEE (commerce 2).
 */
class RetraitServiceTest {

    private RetraitService service;
    private ReservationDao reservationDao;
    private RetraitDao retraitDao;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new RetraitServiceImpl();
        reservationDao = new ReservationDaoImpl();
        retraitDao = new RetraitDaoImpl();
    }

    private String erreur(long commercant, String code) {
        return assertThrows(BusinessException.class, () -> service.validerRetrait(commercant, code)).getMessage();
    }

    @Test
    void validerRetraitCasNominal() {
        Retrait retrait = service.validerRetrait(2L, "A1B2C3");

        assertNotNull(retrait.getId());
        assertNotNull(retrait.getDateRetrait());
        assertEquals(StatutReservation.RETIREE, reservationDao.findById(1L).orElseThrow().getStatut());
        assertTrue(retraitDao.findByReservation(1L).isPresent());
        assertEquals(3, retraitDao.findAll().size()); // 2 retraits de démo + le nouveau
    }

    @Test
    void validerRetraitAccepteLeCodeEnMinusculesAvecDesEspaces() {
        assertNotNull(service.validerRetrait(2L, "  a1b2c3 ").getId());
    }

    @Test
    void validerRetraitRefuseUnCodeInconnuOuVide() {
        assertEquals("Code de retrait invalide", erreur(2L, "ZZZZZZ"));
        assertEquals("Code de retrait invalide", erreur(2L, ""));
        assertEquals("Code de retrait invalide", erreur(2L, null));
    }

    @Test
    void validerRetraitRefuseLeCodeDunAutreCommerce() {
        // A1B2C3 appartient à la boulangerie (commerçant 2), le restaurateur (3) ne peut pas le valider
        assertEquals("Ce code ne correspond à aucun panier de votre commerce", erreur(3L, "A1B2C3"));
        assertEquals(StatutReservation.RESERVEE, reservationDao.findById(1L).orElseThrow().getStatut());
        assertTrue(retraitDao.findByReservation(1L).isEmpty());
    }

    @Test
    void validerRetraitRefuseUneReservationDejaRetiree() {
        assertEquals("Cette réservation n'est plus valide (statut : RETIREE)", erreur(2L, "D4E5F6"));
    }

    @Test
    void validerRetraitRefuseUneReservationAnnulee() {
        assertEquals("Cette réservation n'est plus valide (statut : ANNULEE)", erreur(3L, "K2L3M4"));
    }

    @Test
    void validerDeuxFoisLeMemeCodeEstRefuse() {
        service.validerRetrait(2L, "A1B2C3");
        assertEquals("Cette réservation n'est plus valide (statut : RETIREE)", erreur(2L, "A1B2C3"));
        assertEquals(3, retraitDao.findAll().size()); // un seul retrait créé
    }

    @Test
    void validerRetraitRefuseUnUtilisateurSansCommerce() {
        assertEquals("Aucun commerce n'est associé à votre compte", erreur(5L, "A1B2C3")); // client
        assertEquals("Aucun commerce n'est associé à votre compte", erreur(1L, "A1B2C3")); // admin
    }

    @Test
    void listerRetraitsDuJourContientLeRetraitDuJour() {
        Retrait retrait = service.validerRetrait(2L, "A1B2C3");

        List<Retrait> duJour = service.listerRetraitsDuJour(2L);

        assertTrue(duJour.stream().anyMatch(t -> t.getId().equals(retrait.getId())));
        // le restaurateur ne voit pas les retraits de la boulangerie
        assertTrue(service.listerRetraitsDuJour(3L).stream().noneMatch(t -> t.getId().equals(retrait.getId())));
        assertTrue(service.listerRetraitsDuJour(5L).isEmpty()); // pas de commerce
    }

    @Test
    void parcoursCompletReservationPuisRetrait() {
        // Karim réserve un panier du primeur (commerce 3, commerçant 4)
        Reservation r = new ReservationServiceImpl().reserver(6L, 5L, 2);

        // un autre commerçant ne peut pas valider ce code
        assertEquals("Ce code ne correspond à aucun panier de votre commerce", erreur(2L, r.getCodeRetrait()));

        // le primeur le valide
        service.validerRetrait(4L, r.getCodeRetrait());
        assertEquals(StatutReservation.RETIREE, reservationDao.findById(r.getId()).orElseThrow().getStatut());
    }

    /** Deux commerçants (ou deux clics) valident le même code en même temps : un seul retrait doit être créé. */
    @Test
    void deuxValidationsSimultaneesDuMemeCodeCreentUnSeulRetrait() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch depart = new CountDownLatch(1);
        Callable<Boolean> tache = () -> {
            depart.await();
            try {
                service.validerRetrait(2L, "A1B2C3");
                return true;
            } catch (BusinessException e) {
                return false;
            }
        };
        Future<Boolean> f1 = pool.submit(tache);
        Future<Boolean> f2 = pool.submit(tache);
        depart.countDown();
        int succes = (f1.get(15, TimeUnit.SECONDS) ? 1 : 0) + (f2.get(15, TimeUnit.SECONDS) ? 1 : 0);
        pool.shutdown();

        assertEquals(1, succes);
        assertEquals(3, retraitDao.findAll().size());
    }
}
