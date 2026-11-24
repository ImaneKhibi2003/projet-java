package com.antigaspi.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Supplier;

/**
 * Point d'accès unique à JPA.
 *
 * - Une seule EntityManagerFactory pour toute l'application.
 * - inTransaction(...) ouvre une transaction, la valide (commit) ou l'annule (rollback),
 *   puis ferme l'EntityManager. Si une transaction est déjà en cours sur le thread,
 *   le code la rejoint.
 *
 * Exemple dans un service :
 *   return JpaUtil.inTransaction(() -> {
 *       Panier p = panierDao.findByIdForUpdate(id).orElseThrow();
 *       ...
 *       return reservationDao.save(r);
 *   });
 */
public final class JpaUtil {

    private static final String PERSISTENCE_UNIT = "antigaspiPU";
    private static final ThreadLocal<EntityManager> CURRENT = new ThreadLocal<>();
    private static volatile EntityManagerFactory emf;

    private JpaUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JpaUtil.class) {
                if (emf == null) {
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
                }
            }
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /** EntityManager de la transaction en cours sur ce thread, ou null s'il n'y en a pas. */
    public static EntityManager currentEntityManager() {
        return CURRENT.get();
    }

    public static <R> R inTransaction(Supplier<R> work) {
        if (CURRENT.get() != null) {
            return work.get(); // on rejoint la transaction existante
        }
        EntityManager em = createEntityManager();
        CURRENT.set(em);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = work.get();
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            CURRENT.remove();
            em.close();
        }
    }

    public static void runInTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }

    /** À appeler à l'arrêt de l'application. */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
