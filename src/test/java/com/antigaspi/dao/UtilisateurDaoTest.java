package com.antigaspi.dao;

import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilisateurDaoTest extends AbstractDaoTest {

    private Utilisateur nouveau(String email) {
        Utilisateur u = new Utilisateur();
        u.setNom("Test");
        u.setPrenom("Nouveau");
        u.setEmail(email);
        u.setMotDePasseHash("hash");
        u.setRole(Role.CLIENT);
        return u;
    }

    @Test
    void findAllRetourneLes6UtilisateursDeDemo() {
        assertEquals(6, utilisateurDao.findAll().size());
    }

    @Test
    void findByIdTrouveSara() {
        Optional<Utilisateur> sara = utilisateurDao.findById(5L);
        assertTrue(sara.isPresent());
        assertEquals("sara@antigaspi.ma", sara.get().getEmail());
        assertEquals(Role.CLIENT, sara.get().getRole());
    }

    @Test
    void findByIdInconnuRetourneVide() {
        assertTrue(utilisateurDao.findById(9999L).isEmpty());
    }

    @Test
    void saveCreeUnUtilisateurAvecIdEtDateInscription() {
        Utilisateur cree = utilisateurDao.save(nouveau("nouveau@test.ma"));
        assertNotNull(cree.getId());

        Utilisateur relu = utilisateurDao.findById(cree.getId()).orElseThrow();
        assertNotNull(relu.getDateInscription());
        assertEquals(7, utilisateurDao.findAll().size());
    }

    @Test
    void saveModifieUnUtilisateurExistantEtIncrementeLaVersion() {
        Utilisateur sara = utilisateurDao.findById(5L).orElseThrow();
        long versionAvant = sara.getVersion();
        sara.setNom("Nouveau nom");

        Utilisateur modifie = utilisateurDao.save(sara);

        assertEquals("Nouveau nom", modifie.getNom());
        assertEquals(versionAvant + 1, modifie.getVersion());
        assertEquals("Nouveau nom", utilisateurDao.findById(5L).orElseThrow().getNom());
    }

    @Test
    void deleteByIdSupprimeUnUtilisateurSansDependances() {
        Utilisateur cree = utilisateurDao.save(nouveau("a.supprimer@test.ma"));
        utilisateurDao.deleteById(cree.getId());
        assertTrue(utilisateurDao.findById(cree.getId()).isEmpty());
    }

    @Test
    void findByEmailIgnoreLaCasse() {
        assertTrue(utilisateurDao.findByEmail("ADMIN@ANTIGASPI.MA").isPresent());
        assertTrue(utilisateurDao.findByEmail("inconnu@test.ma").isEmpty());
    }

    @Test
    void existsByEmail() {
        assertTrue(utilisateurDao.existsByEmail("karim@antigaspi.ma"));
        assertFalse(utilisateurDao.existsByEmail("inconnu@test.ma"));
    }

    @Test
    void findByRoleFiltreParRole() {
        assertEquals(1, utilisateurDao.findByRole(Role.ADMIN).size());
        assertEquals(3, utilisateurDao.findByRole(Role.COMMERCANT).size());
        assertEquals(2, utilisateurDao.findByRole(Role.CLIENT).size());
    }

    @Test
    void emailDejaUtiliseEstRefuseParLaBase() {
        assertThrows(PersistenceException.class,
                () -> utilisateurDao.save(nouveau("sara@antigaspi.ma")));
    }

    @Test
    void verrouillageOptimisteRefuseUneModificationPerimee() {
        Utilisateur copie1 = utilisateurDao.findById(5L).orElseThrow();
        Utilisateur copie2 = utilisateurDao.findById(5L).orElseThrow();

        copie1.setNom("Premier");
        utilisateurDao.save(copie1);          // la version passe de 0 à 1

        copie2.setNom("Second");               // copie2 possède encore la version 0
        assertThrows(PersistenceException.class, () -> utilisateurDao.save(copie2));
    }
}
