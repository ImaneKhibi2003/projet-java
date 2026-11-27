package com.antigaspi.dao;

import com.antigaspi.model.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Test manuel : nécessite XAMPP (MySQL démarré) et la base antigaspi importée. Ignoré sinon. */
class MysqlConnexionTest {

    private static boolean mysqlAccessible() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", 3306), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void connexionEtLectureSurMysql() {
        assumeTrue(mysqlAccessible(), "MySQL (XAMPP) n'est pas démarré sur le port 3306 : test ignoré");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("antigaspiMysqlPU");
        try (EntityManager em = emf.createEntityManager()) {
            Long nb = em.createQuery("select count(u) from Utilisateur u", Long.class).getSingleResult();
            assertEquals(6, nb);

            Utilisateur admin = em.createQuery(
                            "select u from Utilisateur u where u.email = 'admin@antigaspi.ma'", Utilisateur.class)
                    .getSingleResult();
            assertEquals("ADMIN", admin.getRole().name());
        } finally {
            emf.close();
        }
    }
}