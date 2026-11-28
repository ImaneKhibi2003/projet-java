package com.antigaspi.service;

import com.antigaspi.model.Commerce;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;

import java.util.List;

public interface UtilisateurService {

    List<Utilisateur> listerTous();

    /** Lève BusinessException("Utilisateur introuvable"). */
    Utilisateur trouverParId(Long id);

    /** Crée un CLIENT ou un ADMIN. Pour un commerçant, utiliser creerCommercant. */
    Utilisateur creer(Utilisateur u, String motDePasse);

    /** AJOUT au contrat : crée un compte COMMERCANT et son commerce dans la même transaction. */
    Utilisateur creerCommercant(Utilisateur u, String motDePasse, Commerce commerce);

    /** Modifie nom, prénom et email (le rôle a sa propre méthode). */
    Utilisateur modifier(Utilisateur u);

    void changerRole(Long utilisateurId, Role nouveauRole);

    void supprimer(Long utilisateurId);
}
