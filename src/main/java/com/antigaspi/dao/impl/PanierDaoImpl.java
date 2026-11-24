package com.antigaspi.dao.impl;

import com.antigaspi.dao.PanierDao;
import com.antigaspi.model.Panier;
import com.antigaspi.model.StatutPanier;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PanierDaoImpl extends AbstractJpaDao<Panier> implements PanierDao {

    public PanierDaoImpl() {
        super(Panier.class, "join fetch e.commerce");
    }

    @Override
    public List<Panier> findDisponibles(String categorie, BigDecimal prixMax) {
        boolean filtreCategorie = categorie != null && !categorie.isBlank();
        boolean filtrePrix = prixMax != null;

        StringBuilder jpql = new StringBuilder(
                "select e from Panier e join fetch e.commerce "
                + "where e.statut = :statut and e.quantiteRestante > 0 and e.dateFinRetrait > :now");
        if (filtreCategorie) {
            jpql.append(" and e.categorie = :categorie");
        }
        if (filtrePrix) {
            jpql.append(" and e.prixReduit <= :prixMax");
        }
        jpql.append(" order by e.dateFinRetrait");

        return lire(em -> {
            TypedQuery<Panier> q = em.createQuery(jpql.toString(), Panier.class)
                    .setParameter("statut", StatutPanier.DISPONIBLE)
                    .setParameter("now", LocalDateTime.now());
            if (filtreCategorie) {
                q.setParameter("categorie", categorie);
            }
            if (filtrePrix) {
                q.setParameter("prixMax", prixMax);
            }
            return q.getResultList();
        });
    }

    @Override
    public List<Panier> findByCommerce(Long commerceId) {
        return lire(em -> em.createQuery(
                        "select e from Panier e join fetch e.commerce "
                        + "where e.commerce.id = :id order by e.dateFinRetrait desc", Panier.class)
                .setParameter("id", commerceId)
                .getResultList());
    }

    @Override
    public List<String> findCategories() {
        return lire(em -> em.createQuery(
                        "select distinct p.categorie from Panier p order by p.categorie", String.class)
                .getResultList());
    }

    @Override
    public Optional<Panier> findByIdForUpdate(Long id) {
        return ecrire(em -> Optional.ofNullable(em.find(Panier.class, id, LockModeType.PESSIMISTIC_WRITE)));
    }
}
