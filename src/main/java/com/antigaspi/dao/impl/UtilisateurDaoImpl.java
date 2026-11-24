package com.antigaspi.dao.impl;

import com.antigaspi.dao.UtilisateurDao;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public class UtilisateurDaoImpl extends AbstractJpaDao<Utilisateur> implements UtilisateurDao {

    public UtilisateurDaoImpl() {
        super(Utilisateur.class, "");
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        return lire(em -> em.createQuery(
                        "select u from Utilisateur u where lower(u.email) = lower(:email)", Utilisateur.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst());
    }

    @Override
    public boolean existsByEmail(String email) {
        return lire(em -> em.createQuery(
                        "select count(u) from Utilisateur u where lower(u.email) = lower(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0);
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        return lire(em -> em.createQuery(
                        "select u from Utilisateur u where u.role = :role order by u.nom, u.prenom", Utilisateur.class)
                .setParameter("role", role)
                .getResultList());
    }
}
