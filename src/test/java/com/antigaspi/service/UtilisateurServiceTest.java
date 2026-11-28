package com.antigaspi.service;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Panier;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.PasswordUtil;
import com.antigaspi.service.impl.AuthServiceImpl;
import com.antigaspi.service.impl.PanierServiceImpl;
import com.antigaspi.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Données de démo : 1 = admin (seul ADMIN), 2-4 = commerçants (chacun un commerce),
 * 5 = Sara, 6 = Karim (clients avec des réservations).
 */
class UtilisateurServiceTest {

    private UtilisateurService service;
    private CommerceDao commerceDao;

    @BeforeEach
    void preparer() {
        JpaUtil.reinitialiserBase();
        service = new UtilisateurServiceImpl();
        commerceDao = new CommerceDaoImpl();
    }

    private Utilisateur modele(String email, Role role) {
        Utilisateur u = new Utilisateur();
        u.setNom("Nouveau");
        u.setPrenom("Utilisateur");
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private Commerce commerce() {
        Commerce c = new Commerce();
        c.setNom("Epicerie Test");
        c.setAdresse("1 rue Test");
        c.setVille("Rabat");
        c.setCategorie("Epicerie");
        return c;
    }

    private String erreur(Runnable action) {
        return assertThrows(BusinessException.class, action::run).getMessage();
    }

    // ---------------------------------------------------------------- lecture

    @Test
    void listerTousEtTrouverParId() {
        assertEquals(6, service.listerTous().size());
        assertEquals("sara@antigaspi.ma", service.trouverParId(5L).getEmail());
        assertEquals("Utilisateur introuvable", erreur(() -> service.trouverParId(9999L)));
        assertEquals("Utilisateur introuvable", erreur(() -> service.trouverParId(null)));
    }

    // ------------------------------------------------------------------ creer

    @Test
    void creerUnClientAvecMotDePasseHacheEtConnexionPossible() {
        Utilisateur cree = service.creer(modele("Client.Neuf@Test.ma", Role.CLIENT), "motdepasse1");

        assertNotNull(cree.getId());
        assertEquals("client.neuf@test.ma", cree.getEmail());
        assertEquals(Role.CLIENT, cree.getRole());
        assertTrue(PasswordUtil.verify("motdepasse1", cree.getMotDePasseHash()));
        assertEquals(cree.getId(), new AuthServiceImpl().login("client.neuf@test.ma", "motdepasse1").getId());
        assertEquals(7, service.listerTous().size());
    }

    @Test
    void creerUnAdministrateur() {
        assertEquals(Role.ADMIN, service.creer(modele("admin2@test.ma", Role.ADMIN), "motdepasse1").getRole());
    }

    @Test
    void creerRefuseLeRoleCommercant() {
        assertEquals("Un compte commerçant doit être créé avec son commerce",
                erreur(() -> service.creer(modele("c@test.ma", Role.COMMERCANT), "motdepasse1")));
    }

    @Test
    void creerRefuseUnEmailDejaUtiliseUnMotDePasseCourtOuUnEmailInvalide() {
        assertEquals("Cet email est déjà utilisé",
                erreur(() -> service.creer(modele("SARA@antigaspi.ma", Role.CLIENT), "motdepasse1")));
        assertEquals("Le mot de passe doit contenir au moins 8 caractères",
                erreur(() -> service.creer(modele("ok@test.ma", Role.CLIENT), "abc")));
        assertEquals("Email invalide",
                erreur(() -> service.creer(modele("pas-un-email", Role.CLIENT), "motdepasse1")));
    }

    // ------------------------------------------------------- creerCommercant

    @Test
    void creerCommercantCreeAussiSonCommerceEtIlPeutPublierUnPanier() {
        Utilisateur cree = service.creerCommercant(modele("marchand@test.ma", Role.CLIENT), "motdepasse1", commerce());

        assertEquals(Role.COMMERCANT, cree.getRole()); // le rôle est forcé
        Commerce c = commerceDao.findByProprietaire(cree.getId()).orElseThrow();
        assertEquals("Epicerie Test", c.getNom());

        Panier p = new Panier();
        p.setTitre("Premier panier");
        p.setCategorie("Epicerie");
        p.setPrixInitial(new BigDecimal("30"));
        p.setPrixReduit(new BigDecimal("10"));
        p.setQuantiteTotale(2);
        p.setDateDebutRetrait(LocalDateTime.now());
        p.setDateFinRetrait(LocalDateTime.now().plusDays(1));
        assertNotNull(new PanierServiceImpl().creer(p, cree.getId()).getId());
    }

    @Test
    void creerCommercantRefuseUnCommerceIncompletEtNeCreeRien() {
        Commerce incomplet = commerce();
        incomplet.setNom("  ");

        assertEquals("Le nom du commerce est obligatoire",
                erreur(() -> service.creerCommercant(modele("marchand@test.ma", Role.COMMERCANT), "motdepasse1", incomplet)));
        assertEquals(6, service.listerTous().size()); // aucun utilisateur créé
        assertEquals("Le commerce est obligatoire pour un commerçant",
                erreur(() -> service.creerCommercant(modele("m2@test.ma", Role.COMMERCANT), "motdepasse1", null)));
    }

    // --------------------------------------------------------------- modifier

    @Test
    void modifierChangeNomPrenomEtEmail() {
        Utilisateur donnees = modele("Sara.Nouvelle@Test.ma", Role.CLIENT);
        donnees.setId(5L);
        donnees.setNom("Benjelloun");

        service.modifier(donnees);

        Utilisateur relu = service.trouverParId(5L);
        assertEquals("Benjelloun", relu.getNom());
        assertEquals("sara.nouvelle@test.ma", relu.getEmail());
        assertEquals(Role.CLIENT, relu.getRole()); // le rôle n'est pas touché
    }

    @Test
    void modifierAutoriseDeGarderSonPropreEmail() {
        Utilisateur donnees = modele("sara@antigaspi.ma", Role.CLIENT);
        donnees.setId(5L);
        assertEquals("Utilisateur", service.modifier(donnees).getPrenom());
    }

    @Test
    void modifierRefuseLEmailDunAutreUtilisateurOuUnUtilisateurInconnu() {
        Utilisateur donnees = modele("karim@antigaspi.ma", Role.CLIENT);
        donnees.setId(5L);
        assertEquals("Cet email est déjà utilisé", erreur(() -> service.modifier(donnees)));
        assertEquals("sara@antigaspi.ma", service.trouverParId(5L).getEmail()); // inchangé

        Utilisateur inconnu = modele("x@test.ma", Role.CLIENT);
        inconnu.setId(9999L);
        assertEquals("Utilisateur introuvable", erreur(() -> service.modifier(inconnu)));
    }

    // ------------------------------------------------------------ changerRole

    @Test
    void changerRoleDunClientEnAdmin() {
        service.changerRole(5L, Role.ADMIN);
        assertEquals(Role.ADMIN, service.trouverParId(5L).getRole());
    }

    @Test
    void changerVersLeMemeRoleNeFaitRien() {
        service.changerRole(5L, Role.CLIENT);
        assertEquals(Role.CLIENT, service.trouverParId(5L).getRole());
    }

    @Test
    void changerRoleRefuseDeRetirerLeDernierAdmin() {
        assertEquals("Impossible de retirer le rôle du dernier administrateur",
                erreur(() -> service.changerRole(1L, Role.CLIENT)));

        service.changerRole(5L, Role.ADMIN); // un second admin existe : l'opération devient possible
        service.changerRole(1L, Role.CLIENT);
        assertEquals(Role.CLIENT, service.trouverParId(1L).getRole());
    }

    @Test
    void changerRoleProtegeLesCommercantsEtExigeUnCommerce() {
        assertEquals("Cet utilisateur possède un commerce : son rôle ne peut pas être modifié",
                erreur(() -> service.changerRole(2L, Role.CLIENT)));
        assertEquals("Un commerçant doit avoir un commerce : créez un compte commerçant avec son commerce",
                erreur(() -> service.changerRole(5L, Role.COMMERCANT)));
        assertEquals(Role.CLIENT, service.trouverParId(5L).getRole());
    }

    @Test
    void changerRoleRefuseUnUtilisateurInconnuOuUnRoleNul() {
        assertEquals("Utilisateur introuvable", erreur(() -> service.changerRole(9999L, Role.ADMIN)));
        assertEquals("Rôle invalide", erreur(() -> service.changerRole(5L, null)));
    }

    // -------------------------------------------------------------- supprimer

    @Test
    void supprimerUnUtilisateurSansHistorique() {
        Utilisateur cree = service.creer(modele("a.supprimer@test.ma", Role.CLIENT), "motdepasse1");
        service.supprimer(cree.getId());
        assertEquals("Utilisateur introuvable", erreur(() -> service.trouverParId(cree.getId())));
        assertEquals(6, service.listerTous().size());
    }

    @Test
    void supprimerRefuseUnClientAvecDesReservations() {
        assertEquals("Cet utilisateur a des réservations : il ne peut pas être supprimé",
                erreur(() -> service.supprimer(5L)));
        assertNotNull(service.trouverParId(5L));
    }

    @Test
    void supprimerRefuseUnCommercantQuiPossedeUnCommerce() {
        assertEquals("Cet utilisateur possède un commerce : il ne peut pas être supprimé",
                erreur(() -> service.supprimer(2L)));
    }

    @Test
    void supprimerRefuseLeDernierAdminPuisAutoriseQuandIlEnExisteUnAutre() {
        assertEquals("Impossible de supprimer le dernier administrateur", erreur(() -> service.supprimer(1L)));

        service.creer(modele("admin2@test.ma", Role.ADMIN), "motdepasse1");
        service.supprimer(1L);
        assertEquals("Utilisateur introuvable", erreur(() -> service.trouverParId(1L)));
    }

    @Test
    void supprimerRefuseUnUtilisateurInconnu() {
        assertEquals("Utilisateur introuvable", erreur(() -> service.supprimer(9999L)));
    }
}
