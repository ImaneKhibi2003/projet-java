package com.antigaspi.service.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.RetraitDao;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.RetraitDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.exception.TechnicalException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.Retrait;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.service.RetraitService;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VERSION PROVISOIRE écrite par A pour débloquer les servlets (A4.8).
 * La personne B la reprend ou l'améliore (tâche B3.4) en gardant les signatures.
 */
public class RetraitServiceImpl implements RetraitService {

    private static final String MSG_CODE_INVALIDE = "Code de retrait invalide";

    private final CommerceDao commerceDao;
    private final ReservationDao reservationDao;
    private final RetraitDao retraitDao;

    public RetraitServiceImpl() {
        this(new CommerceDaoImpl(), new ReservationDaoImpl(), new RetraitDaoImpl());
    }

    public RetraitServiceImpl(CommerceDao commerceDao, ReservationDao reservationDao, RetraitDao retraitDao) {
        this.commerceDao = commerceDao;
        this.reservationDao = reservationDao;
        this.retraitDao = retraitDao;
    }

    @Override
    public Retrait validerRetrait(Long commercantId, String codeRetrait) {
        String code = normaliser(codeRetrait);
        try {
            return JpaUtil.inTransaction(() -> {
                if (code.isEmpty()) {
                    throw new BusinessException(MSG_CODE_INVALIDE);
                }
                Reservation reservation = reservationDao.findByCodeRetrait(code)
                        .orElseThrow(() -> new BusinessException(MSG_CODE_INVALIDE));

                Commerce commerce = commerceDao.findByProprietaire(commercantId)
                        .orElseThrow(() -> new BusinessException("Aucun commerce n'est associé à votre compte"));
                // Le propriétaire est vérifié avant le statut : on ne révèle rien sur les réservations des autres
                if (!reservation.getPanier().getCommerce().getId().equals(commerce.getId())) {
                    throw new BusinessException("Ce code ne correspond à aucun panier de votre commerce");
                }
                if (reservation.getStatut() != StatutReservation.RESERVEE) {
                    throw new BusinessException("Cette réservation n'est plus valide (statut : "
                            + reservation.getStatut() + ")");
                }

                Retrait retrait = new Retrait();
                retrait.setReservation(reservation);
                reservation.setStatut(StatutReservation.RETIREE);
                return retraitDao.save(retrait);
            });
        } catch (PersistenceException e) {
            // Deux validations simultanées du même code : le verrouillage optimiste (@Version) et la contrainte
            // UNIQUE sur retrait.reservation_id en laissent passer une seule.
            Optional<Reservation> relue = reservationDao.findByCodeRetrait(code);
            if (relue.isPresent() && relue.get().getStatut() == StatutReservation.RETIREE) {
                throw new BusinessException("Cette réservation n'est plus valide (statut : "
                        + StatutReservation.RETIREE + ")");
            }
            throw new TechnicalException("Échec de la validation du retrait", e);
        }
    }

    @Override
    public List<Retrait> listerRetraitsDuJour(Long commercantId) {
        return commerceDao.findByProprietaire(commercantId)
                .map(commerce -> retraitDao.findByCommerceAndDate(commerce.getId(), LocalDate.now()))
                .orElse(List.of());
    }

    private static String normaliser(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}
