package com.antigaspi.dao.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.model.Commerce;

import java.util.Optional;

public class CommerceDaoImpl extends AbstractJpaDao<Commerce> implements CommerceDao {

    public CommerceDaoImpl() {
        super(Commerce.class, "join fetch e.proprietaire");
    }

    @Override
    public Optional<Commerce> findByProprietaire(Long utilisateurId) {
        return lire(em -> em.createQuery(
                        "select e from Commerce e join fetch e.proprietaire where e.proprietaire.id = :id",
                        Commerce.class)
                .setParameter("id", utilisateurId)
                .getResultStream()
                .findFirst());
    }
}
