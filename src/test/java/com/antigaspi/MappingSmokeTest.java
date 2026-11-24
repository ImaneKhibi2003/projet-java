package com.antigaspi;

import com.antigaspi.model.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingSmokeTest {

    @Test
    void chargeLesDonneesDeDemo() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("antigaspiPU");
        EntityManager em = emf.createEntityManager();

        Long nb = em.createQuery("select count(u) from Utilisateur u", Long.class).getSingleResult();
        assertEquals(6, nb);

        Utilisateur admin = em.createQuery(
                        "select u from Utilisateur u where u.email = 'admin@antigaspi.ma'", Utilisateur.class)
                .getSingleResult();
        assertEquals("ADMIN", admin.getRole().name());

        em.close();
        emf.close();
    }
}