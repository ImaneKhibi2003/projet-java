package com.antigaspi.dao.impl;

import com.antigaspi.dao.RetraitDao;
import com.antigaspi.model.Retrait;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RetraitDaoImpl extends AbstractJpaDao<Retrait> implements RetraitDao {

    // Jointures partagées : réservation, panier, commerce et client
    private static final String FETCH =
            "join fetch e.reservation r join fetch r.panier p join fetch p.commerce join fetch r.utilisateur";

    public RetraitDaoImpl() {
        super(Retrait.class, FETCH);
    }

    @Override
    public Optional<Retrait> findByReservation(Long reservationId) {
        return lire(em -> em.createQuery(
                        "select e from Retrait e " + FETCH + " where r.id = :id", Retrait.class)
                .setParameter("id", reservationId)
                .getResultStream()
                .findFirst());
    }

    @Override
    public List<Retrait> findByCommerceAndDate(Long commerceId, LocalDate jour) {
        return lire(em -> em.createQuery(
                        "select e from Retrait e " + FETCH
                        + " where p.commerce.id = :id and e.dateRetrait >= :debut and e.dateRetrait < :fin"
                        + " order by e.dateRetrait desc", Retrait.class)
                .setParameter("id", commerceId)
                .setParameter("debut", jour.atStartOfDay())
                .setParameter("fin", jour.plusDays(1).atStartOfDay())
                .getResultList());
    }
}
