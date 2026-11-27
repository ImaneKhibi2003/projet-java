package com.antigaspi.dao;

import com.antigaspi.model.Commerce;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommerceDaoTest extends AbstractDaoTest {

    @Test
    void findAllRetourneLes3Commerces() {
        assertEquals(3, commerceDao.findAll().size());
    }

    @Test
    void findByIdChargeLeProprietaire() {
        Commerce c = commerceDao.findById(1L).orElseThrow();
        assertEquals("Boulangerie Al Baraka", c.getNom());
        // accessible même si l'EntityManager est fermé (join fetch)
        assertEquals("boulangerie@antigaspi.ma", c.getProprietaire().getEmail());
    }

    @Test
    void findByProprietaire() {
        Commerce c = commerceDao.findByProprietaire(3L).orElseThrow();
        assertEquals("Restaurant Dar Zitoun", c.getNom());
        assertTrue(commerceDao.findByProprietaire(5L).isEmpty()); // Sara n'a pas de commerce
    }

    @Test
    void saveCreeUnCommerce() {
        Utilisateur proprio = new Utilisateur();
        proprio.setNom("Nouveau");
        proprio.setPrenom("Commercant");
        proprio.setEmail("commercant@test.ma");
        proprio.setMotDePasseHash("hash");
        proprio.setRole(Role.COMMERCANT);
        proprio = utilisateurDao.save(proprio);

        Commerce c = new Commerce();
        c.setNom("Epicerie Test");
        c.setAdresse("1 rue Test");
        c.setVille("Rabat");
        c.setCategorie("Epicerie");
        c.setProprietaire(proprio);

        Commerce cree = commerceDao.save(c);

        assertNotNull(cree.getId());
        assertEquals(4, commerceDao.findAll().size());
    }

    @Test
    void saveModifieUnCommerce() {
        Commerce c = commerceDao.findById(1L).orElseThrow();
        c.setVille("Rabat");
        commerceDao.save(c);
        assertEquals("Rabat", commerceDao.findById(1L).orElseThrow().getVille());
    }

    @Test
    void deleteByIdSupprimeUnCommerce() {
        commerceDao.deleteById(3L); // supprime aussi ses paniers (ON DELETE CASCADE)
        assertTrue(commerceDao.findById(3L).isEmpty());
        assertEquals(2, commerceDao.findAll().size());
        assertTrue(panierDao.findByCommerce(3L).isEmpty());
    }
}
