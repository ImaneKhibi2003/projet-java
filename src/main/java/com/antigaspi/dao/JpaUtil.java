package com.antigaspi.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.h2.tools.RunScript;
import org.hibernate.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Point d'accès unique à JPA.
 *
 * - Une seule EntityManagerFactory pour toute l'application.
 * - inTransaction(...) ouvre une transaction, la valide (commit) ou l'annule (rollback),
 *   puis ferme l'EntityManager. Si une transaction est déjà en cours sur le thread,
 *   le code la rejoint.
 * - Avec H2 en mémoire, schema.sql puis data.sql sont exécutés UNE SEULE FOIS au démarrage.
 *   reinitialiserBase() permet de remettre les données de démo (utile pour les tests).
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
                    EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
                    if (estH2EnMemoire(factory)) {
                        executerScripts(factory);
                    }
                    emf = factory;
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

    /** Recrée les tables et recharge les données de démo (H2 en mémoire uniquement). */
    public static void reinitialiserBase() {
        EntityManagerFactory factory = getEntityManagerFactory();
        if (!estH2EnMemoire(factory)) {
            throw new IllegalStateException("reinitialiserBase() n'est permis qu'avec H2 en mémoire");
        }
        executerScripts(factory);
    }

    /** À appeler à l'arrêt de l'application. */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    // ------------------------------------------------------------------

    private static boolean estH2EnMemoire(EntityManagerFactory factory) {
        Object url = factory.getProperties().get("jakarta.persistence.jdbc.url");
        return url != null && url.toString().startsWith("jdbc:h2:mem:");
    }

    private static void executerScripts(EntityManagerFactory factory) {
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.unwrap(Session.class).doWork(connection -> {
                try {
                    executerScript(connection, "/schema.sql");
                    executerScript(connection, "/data.sql");
                } catch (IOException e) {
                    throw new IllegalStateException("Lecture des scripts SQL impossible", e);
                }
            });
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static void executerScript(java.sql.Connection connection, String ressource)
            throws IOException, java.sql.SQLException {
        try (InputStream in = JpaUtil.class.getResourceAsStream(ressource)) {
            if (in == null) {
                throw new IllegalStateException("Script introuvable dans le classpath : " + ressource);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
    }
}