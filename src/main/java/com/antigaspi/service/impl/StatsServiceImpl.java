package com.antigaspi.service.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.PanierDao;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.RetraitDao;
import com.antigaspi.dao.UtilisateurDao;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.RetraitDaoImpl;
import com.antigaspi.dao.impl.UtilisateurDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.service.StatsService;
import com.antigaspi.service.dto.StatsAdmin;
import com.antigaspi.service.dto.StatsCommercant;

import java.math.BigDecimal;
import java.util.List;

/**
 * VERSION PROVISOIRE écrite par A pour les tableaux de bord (A4.6, A4.9).
 * Définitions : invendusSauves = somme des quantités des réservations RETIREE ;
 * chiffreAffaires = somme de prixReduit x quantite des réservations RETIREE.
 */
public class StatsServiceImpl implements StatsService {

    private final UtilisateurDao utilisateurDao;
    private final CommerceDao commerceDao;
    private final PanierDao panierDao;
    private final ReservationDao reservationDao;
    private final RetraitDao retraitDao;

    public StatsServiceImpl() {
        this(new UtilisateurDaoImpl(), new CommerceDaoImpl(), new PanierDaoImpl(),
                new ReservationDaoImpl(), new RetraitDaoImpl());
    }

    public StatsServiceImpl(UtilisateurDao utilisateurDao, CommerceDao commerceDao, PanierDao panierDao,
                            ReservationDao reservationDao, RetraitDao retraitDao) {
        this.utilisateurDao = utilisateurDao;
        this.commerceDao = commerceDao;
        this.panierDao = panierDao;
        this.reservationDao = reservationDao;
        this.retraitDao = retraitDao;
    }

    @Override
    public StatsCommercant statsCommercant(Long commercantId) {
        Commerce commerce = commerceDao.findByProprietaire(commercantId)
                .orElseThrow(() -> new BusinessException("Aucun commerce n'est associé à votre compte"));
        int nbPaniers = panierDao.findByCommerce(commerce.getId()).size();
        List<Reservation> reservations = reservationDao.findByCommerce(commerce.getId());
        return new StatsCommercant(nbPaniers, reservations.size(),
                chiffreAffaires(reservations), invendusSauves(reservations));
    }

    @Override
    public StatsAdmin statsAdmin() {
        List<Reservation> reservations = reservationDao.findAll();
        return new StatsAdmin(
                utilisateurDao.findAll().size(),
                commerceDao.findAll().size(),
                panierDao.findAll().size(),
                reservations.size(),
                retraitDao.findAll().size(),
                chiffreAffaires(reservations),
                invendusSauves(reservations));
    }

    private static BigDecimal chiffreAffaires(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.RETIREE)
                .map(r -> r.getPanier().getPrixReduit().multiply(BigDecimal.valueOf(r.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int invendusSauves(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.RETIREE)
                .mapToInt(Reservation::getQuantite)
                .sum();
    }
}
