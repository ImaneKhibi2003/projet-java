package com.antigaspi.dao.impl;

import com.antigaspi.dao.CrudDao;
import com.antigaspi.dao.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Base commune des DAO JPA.
 *
 * - Lecture : utilise l'EntityManager de la transaction en cours, sinon en ouvre un temporaire.
 * - Écriture : s'exécute toujours dans une transaction (rejoint celle du service si elle existe).
 *
 * fetchJoins : jointures "join fetch" appliquées à findById/findAll, pour que les JSP
 * puissent lire les objets liés (commerce, client...) même après la fermeture de l'EntityManager.
 * L'alias de l'entité principale est toujours "e".
 */
public abstract class AbstractJpaDao<T> implements CrudDao<T, Long> {

    protected final Class<T> entityClass;
    private final String fetchJoins;

    protected AbstractJpaDao(Class<T> entityClass, String fetchJoins) {
        this.entityClass = entityClass;
        this.fetchJoins = fetchJoins;
    }

    protected <R> R lire(Function<EntityManager, R> work) {
        EntityManager current = JpaUtil.currentEntityManager();
        if (current != null) {
            return work.apply(current);
        }
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    protected <R> R ecrire(Function<EntityManager, R> work) {
        return JpaUtil.inTransaction(() -> work.apply(JpaUtil.currentEntityManager()));
    }

    @Override
    public T save(T entity) {
        return ecrire(em -> {
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            if (id == null) {
                em.persist(entity);
                return entity;
            }
            return em.merge(entity);
        });
    }

    @Override
    public Optional<T> findById(Long id) {
        return lire(em -> em.createQuery(
                        "select e from " + entityClass.getSimpleName() + " e " + fetchJoins + " where e.id = :id",
                        entityClass)
                .setParameter("id", id)
                .getResultStream()
                .findFirst());
    }

    @Override
    public List<T> findAll() {
        return lire(em -> em.createQuery(
                        "select e from " + entityClass.getSimpleName() + " e " + fetchJoins + " order by e.id",
                        entityClass)
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        ecrire(em -> {
            T ref = em.find(entityClass, id);
            if (ref != null) {
                em.remove(ref);
            }
            return null;
        });
    }
}
