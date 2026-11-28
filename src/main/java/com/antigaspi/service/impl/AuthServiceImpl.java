package com.antigaspi.service.impl;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.UtilisateurDao;
import com.antigaspi.dao.impl.UtilisateurDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.exception.TechnicalException;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.PasswordUtil;
import com.antigaspi.service.AuthService;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * VERSION PROVISOIRE écrite par A pour débloquer les servlets (A4.1).
 * La personne B la remplace ou l'améliore (tâche B3.9) en gardant les signatures de AuthService.
 */
public class AuthServiceImpl implements AuthService {

    private static final String MSG_IDENTIFIANTS = "Email ou mot de passe incorrect";
    private static final String MSG_EMAIL_PRIS = "Cet email est déjà utilisé";
    private static final int LONGUEUR_MIN_MOT_DE_PASSE = 8;
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final UtilisateurDao utilisateurDao;

    public AuthServiceImpl() {
        this(new UtilisateurDaoImpl());
    }

    public AuthServiceImpl(UtilisateurDao utilisateurDao) {
        this.utilisateurDao = utilisateurDao;
    }

    @Override
    public Utilisateur login(String email, String motDePasse) {
        String emailNormalise = normaliser(email);
        if (emailNormalise.isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            throw new BusinessException(MSG_IDENTIFIANTS);
        }
        Utilisateur utilisateur = utilisateurDao.findByEmail(emailNormalise)
                .orElseThrow(() -> new BusinessException(MSG_IDENTIFIANTS));
        if (!PasswordUtil.verify(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new BusinessException(MSG_IDENTIFIANTS);
        }
        return utilisateur;
    }

    @Override
    public Utilisateur register(String nom, String prenom, String email, String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            throw new BusinessException("Le mot de passe doit contenir au moins "
                    + LONGUEUR_MIN_MOT_DE_PASSE + " caractères");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom == null ? "" : nom.trim());
        utilisateur.setPrenom(prenom == null ? "" : prenom.trim());
        utilisateur.setEmail(normaliser(email));
        utilisateur.setRole(Role.CLIENT);
        utilisateur.setMotDePasseHash(PasswordUtil.hash(motDePasse));

        Set<ConstraintViolation<Utilisateur>> violations = VALIDATOR.validate(utilisateur);
        if (!violations.isEmpty()) {
            throw new BusinessException(violations.iterator().next().getMessage());
        }

        try {
            return JpaUtil.inTransaction(() -> {
                if (utilisateurDao.existsByEmail(utilisateur.getEmail())) {
                    throw new BusinessException(MSG_EMAIL_PRIS);
                }
                return utilisateurDao.save(utilisateur);
            });
        } catch (PersistenceException e) {
            // deux inscriptions simultanées avec le même email : la contrainte UNIQUE de la base protège
            if (utilisateurDao.existsByEmail(utilisateur.getEmail())) {
                throw new BusinessException(MSG_EMAIL_PRIS);
            }
            throw new TechnicalException("Échec de l'inscription", e);
        }
    }

    private static String normaliser(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
