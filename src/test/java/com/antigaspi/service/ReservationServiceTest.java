package com.antigaspi.service;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.PanierDao;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.StatutPanier;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jeu de données (data.sql) : Sara = 5, Karim = 6, admin = 1, commerçant = 2.
 * Panier 1 : 4 restants (Sara a une réservation RESERVEE), panier 4 : 3 restants,
 * panier 5 : 6 restants, panier 6 : expiré. Suppose une date du jour avant le 31/12/2026.
 */
class ReservationServiceTest {

    private ReservationService service;
    private PanierDao panierDao;
    private ReservationDao reservationDao;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new ReservationServiceImpl();
        panierDao = new PanierDaoImpl();
        reservationDao = new ReservationDaoImpl();
    }

    private int stock(long panierId) {
        return panierDao.findById(panierId).orElseThrow().getQuantiteRestante();
    }

    private String erreurReserver(long client, long panier, int quantite) {
        return assertThrows(BusinessException.class, () -> service.reserver(client, panier, quantite)).getMessage();
    }

    private String erreurAnnuler(long client, long reservation) {
        return assertThrows(BusinessException.class, () -> service.annuler(client, reservation)).getMessage();
    }

    // ------------------------------------------------------------ reserver

    @Test
    void reserverCasNominal() {
        Reservation r = service.reserver(5L, 5L, 2);

        assertNotNull(r.getId());
        assertEquals(StatutReservation.RESERVEE, r.getStatut());
        assertEquals(2, r.getQuantite());
        assertTrue(r.getCodeRetrait().matches("[A-HJ-NP-Z2-9]{6}"));
        assertEquals(4, stock(5L)); // 6 - 2

        Reservation relue = reservationDao.findById(r.getId()).orElseThrow();
        assertEquals("sara@antigaspi.ma", relue.getUtilisateur().getEmail());
    }

    @Test
    void reserverLeDernierStockPasseLePanierAEpuise() {
        service.reserver(5L, 4L, 3); // le panier 4 en a 3

        Panier p = panierDao.findById(4L).orElseThrow();
        assertEquals(0, p.getQuantiteRestante());
        assertEquals(StatutPanier.EPUISE, p.getStatut());
    }

    @Test
    void reserverRefuseUneQuantiteSuperieureAuStock() {
        assertEquals("Quantité insuffisante : il reste 4 panier(s)", erreurReserver(5L, 1L, 5));
        // le stock n'a pas bougé
        assertEquals(4, stock(1L));
    }

    @Test
    void reserverRefuseUneQuantiteNulle() {
        assertEquals("La quantité doit être au moins 1", erreurReserver(5L, 5L, 0));
        assertEquals(6, stock(5L));
    }

    @Test
    void reserverRefuseUnDoublon() {
        // Sara a déjà une réservation RESERVEE sur le panier 1
        assertEquals("Vous avez déjà une réservation en cours pour ce panier", erreurReserver(5L, 1L, 1));
        assertEquals(4, stock(1L));
    }

    @Test
    void reserverAutoriseUnNouveauClientSurUnPanierDejaReserveParUnAutre() {
        Reservation r = service.reserver(6L, 1L, 1); // Karim sur le panier 1
        assertNotNull(r.getId());
        assertEquals(3, stock(1L));
    }

    @Test
    void reserverRefuseUnPanierExpire() {
        assertEquals("Ce panier n'est plus disponible", erreurReserver(5L, 6L, 1));
    }

    @Test
    void reserverRefuseUnPanierDontLaDateDeFinEstDepassee() {
        Panier p = panierDao.findById(5L).orElseThrow();
        p.setDateDebutRetrait(LocalDateTime.now().minusDays(2));
        p.setDateFinRetrait(LocalDateTime.now().minusDays(1));
        panierDao.save(p); // statut toujours DISPONIBLE : l'expiration n'a pas encore été traitée

        assertEquals("Ce panier est expiré", erreurReserver(5L, 5L, 1));
    }

    @Test
    void reserverRefuseLesNonClients() {
        assertEquals("Seuls les clients peuvent réserver", erreurReserver(1L, 5L, 1)); // admin
        assertEquals("Seuls les clients peuvent réserver", erreurReserver(2L, 5L, 1)); // commerçant
    }

    @Test
    void reserverRefuseUnPanierOuUnClientInconnu() {
        assertEquals("Panier introuvable", erreurReserver(5L, 9999L, 1));
        assertEquals("Utilisateur introuvable", erreurReserver(9999L, 5L, 1));
    }

    @Test
    void lesCodesDeRetraitSontUniques() {
        Set<String> codes = new HashSet<>();
        codes.add(service.reserver(5L, 5L, 1).getCodeRetrait());
        codes.add(service.reserver(6L, 5L, 1).getCodeRetrait());
        codes.add(service.reserver(5L, 4L, 1).getCodeRetrait());
        assertEquals(3, codes.size());
    }

    /**
     * Test de concurrence : il ne reste qu'un panier, deux clients le demandent en même temps.
     * Grâce au verrou PESSIMISTIC_WRITE, un seul doit réussir.
     */
    @Test
    void deuxClientsSeDisputentLeDernierPanier() throws Exception {
        Panier p = panierDao.findById(5L).orElseThrow();
        p.setQuantiteRestante(1);
        panierDao.save(p);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch depart = new CountDownLatch(1);
        Callable<Boolean> sara = tentative(depart, 5L);
        Callable<Boolean> karim = tentative(depart, 6L);

        Future<Boolean> f1 = pool.submit(sara);
        Future<Boolean> f2 = pool.submit(karim);
        depart.countDown();
        int succes = (f1.get(15, TimeUnit.SECONDS) ? 1 : 0) + (f2.get(15, TimeUnit.SECONDS) ? 1 : 0);
        pool.shutdown();

        assertEquals(1, succes, "un seul client doit obtenir le dernier panier");
        Panier apres = panierDao.findById(5L).orElseThrow();
        assertEquals(0, apres.getQuantiteRestante());
        assertEquals(StatutPanier.EPUISE, apres.getStatut());
    }

    private Callable<Boolean> tentative(CountDownLatch depart, long clientId) {
        return () -> {
            depart.await();
            try {
                service.reserver(clientId, 5L, 1);
                return true;
            } catch (BusinessException e) {
                return false;
            }
        };
    }

    // ------------------------------------------------------------- annuler

    @Test
    void annulerRestitueLeStockEtPasseLaReservationAAnnulee() {
        service.annuler(5L, 1L); // réservation 1 : Sara, panier 1, quantité 1

        assertEquals(5, stock(1L)); // 4 + 1
        assertEquals(StatutReservation.ANNULEE, reservationDao.findById(1L).orElseThrow().getStatut());
    }

    @Test
    void annulerRepasseUnPanierEpuiseADisponible() {
        Reservation r = service.reserver(5L, 4L, 3); // panier 4 épuisé
        assertEquals(StatutPanier.EPUISE, panierDao.findById(4L).orElseThrow().getStatut());

        service.annuler(5L, r.getId());

        Panier p = panierDao.findById(4L).orElseThrow();
        assertEquals(StatutPanier.DISPONIBLE, p.getStatut());
        assertEquals(3, p.getQuantiteRestante());
    }

    @Test
    void annulerRefuseLaReservationDunAutreClient() {
        assertEquals("Cette réservation ne vous appartient pas", erreurAnnuler(6L, 1L)); // Karim, résa de Sara
        assertEquals(StatutReservation.RESERVEE, reservationDao.findById(1L).orElseThrow().getStatut());
        assertEquals(4, stock(1L));
    }

    @Test
    void annulerRefuseUneReservationDejaRetireeOuAnnulee() {
        assertEquals("Seule une réservation en cours peut être annulée", erreurAnnuler(5L, 2L)); // RETIREE
        assertEquals("Seule une réservation en cours peut être annulée", erreurAnnuler(6L, 4L)); // ANNULEE
    }

    @Test
    void annulerDeuxFoisLaMemeReservationEstRefuse() {
        service.annuler(5L, 1L);
        assertEquals("Seule une réservation en cours peut être annulée", erreurAnnuler(5L, 1L));
        assertEquals(5, stock(1L)); // le stock n'a été rendu qu'une fois
    }

    @Test
    void annulerUneReservationInconnue() {
        assertEquals("Réservation introuvable", erreurAnnuler(5L, 9999L));
    }

    // -------------------------------------------------------------- listes

    @Test
    void listerParClientEtParCommerce() {
        assertEquals(2, service.listerParClient(5L).size());
        assertEquals(2, service.listerParCommerce(1L).size());
    }

    // ------------------------------------------------------ CodeGenerator

    @Test
    void codeGeneratorProduitDesCodesValides() {
        CodeGenerator generateur = new CodeGenerator();
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String code = generateur.generer();
            assertTrue(code.matches("[A-HJ-NP-Z2-9]{6}"), code);
            codes.add(code);
        }
        assertEquals(200, codes.size());
    }
}
