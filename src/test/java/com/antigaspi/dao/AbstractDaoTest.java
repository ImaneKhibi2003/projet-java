package com.antigaspi.dao;

import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.RetraitDaoImpl;
import com.antigaspi.dao.impl.UtilisateurDaoImpl;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base des tests DAO : avant chaque test, les tables sont recréées avec les données de data.sql.
 * Repères du jeu de données :
 *  utilisateurs : 1 admin, 2-4 commerçants, 5 Sara (client), 6 Karim (client)
 *  commerces    : 1 boulangerie (resp. 2), 2 restaurant (resp. 3), 3 primeur (resp. 4)
 *  paniers      : 1-2 commerce 1, 3-4 commerce 2, 5-6 commerce 3 (le 6 est expiré)
 *  réservations : 1 RESERVEE (code A1B2C3), 2 RETIREE, 3 RETIREE, 4 ANNULEE
 */
abstract class AbstractDaoTest {

    protected UtilisateurDao utilisateurDao;
    protected CommerceDao commerceDao;
    protected PanierDao panierDao;
    protected ReservationDao reservationDao;
    protected RetraitDao retraitDao;

    @BeforeEach
    void reinitialiser() {
        JpaUtil.reinitialiserBase();
        utilisateurDao = new UtilisateurDaoImpl();
        commerceDao = new CommerceDaoImpl();
        panierDao = new PanierDaoImpl();
        reservationDao = new ReservationDaoImpl();
        retraitDao = new RetraitDaoImpl();
    }
}
