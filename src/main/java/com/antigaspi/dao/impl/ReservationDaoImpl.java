package com.antigaspi.dao.impl;

import com.antigaspi.dao.ReservationDao;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.StatutReservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationDaoImpl extends AbstractJpaDao<Reservation> implements ReservationDao {

    // Jointures partagées : panier, commerce du panier et client
    private static final String FETCH =
            "join fetch e.panier p join fetch p.commerce join fetch e.utilisateur";

    public ReservationDaoImpl() {
        super(Reservation.class, FETCH);
    }

    @Override
    public List<Reservation> findByUtilisateur(Long utilisateurId) {
        return lire(em -> em.createQuery(
                        "select e from Reservation e " + FETCH
                        + " where e.utilisateur.id = :id order by e.dateReservation desc", Reservation.class)
                .setParameter("id", utilisateurId)
                .getResultList());
    }

    @Override
    public List<Reservation> findByCommerce(Long commerceId) {
        return lire(em -> em.createQuery(
                        "select e from Reservation e " + FETCH
                        + " where p.commerce.id = :id order by e.dateReservation desc", Reservation.class)
                .setParameter("id", commerceId)
                .getResultList());
    }

    @Override
    public Optional<Reservation> findByCodeRetrait(String code) {
        return lire(em -> em.createQuery(
                        "select e from Reservation e " + FETCH + " where e.codeRetrait = :code", Reservation.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst());
    }

    @Override
    public boolean existsByCodeRetrait(String code) {
        return lire(em -> em.createQuery(
                        "select count(r) from Reservation r where r.codeRetrait = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsActiveByUtilisateurAndPanier(Long utilisateurId, Long panierId) {
        return lire(em -> em.createQuery(
                        "select count(r) from Reservation r "
                        + "where r.utilisateur.id = :u and r.panier.id = :p and r.statut = :statut", Long.class)
                .setParameter("u", utilisateurId)
                .setParameter("p", panierId)
                .setParameter("statut", StatutReservation.RESERVEE)
                .getSingleResult() > 0);
    }

    @Override
    public List<Reservation> findExpirees() {
        return lire(em -> em.createQuery(
                        "select e from Reservation e " + FETCH
                        + " where e.statut = :statut and p.dateFinRetrait < :now", Reservation.class)
                .setParameter("statut", StatutReservation.RESERVEE)
                .setParameter("now", LocalDateTime.now())
                .getResultList());
    }
}
