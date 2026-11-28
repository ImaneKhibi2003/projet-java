package com.antigaspi.service;

import com.antigaspi.model.Utilisateur;

public interface AuthService {

    /** Lève BusinessException("Email ou mot de passe incorrect"). */
    Utilisateur login(String email, String motDePasse);

    /** Rôle CLIENT par défaut. Lève BusinessException si l'email est déjà utilisé. */
    Utilisateur register(String nom, String prenom, String email, String motDePasse);
}
