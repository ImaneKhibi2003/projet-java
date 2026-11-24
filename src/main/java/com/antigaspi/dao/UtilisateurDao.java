package com.antigaspi.dao;

import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurDao extends CrudDao<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(Role role);
}
