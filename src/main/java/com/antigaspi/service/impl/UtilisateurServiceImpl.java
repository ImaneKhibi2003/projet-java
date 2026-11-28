package com.antigaspi.service.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.UtilisateurDao;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.UtilisateurDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.exception.TechnicalException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Role;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.security.PasswordUtil;
import com.antigaspi.service.UtilisateurService;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;

/**
 * VERSION PROVISOIRE écrite par A pour l'administration (A4.9, A4.10).
 * La personne B la reprend ou l'améliore (tâche B3.1) en gardant les signatures.
 *
 * Règles d'intégrité :
 *  - un compte COMMERCANT est toujours lié à un commerce ;
 *  - on ne supprime pas un utilisateur qui possède un commerce ou a des réservations (historique et statistiques) ;
 *  - il reste toujours au moins un ADMIN.
 */
public class UtilisateurServiceImpl implements UtilisateurService {

    private static final String MSG_INTROUVABLE = "Utilisateur introuvable";
    private static final String MSG_EMAIL_PRIS = "Cet email est déjà utilisé";
    private static final int LONGUEUR_MIN_MOT_DE_PASSE = 8;
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final UtilisateurDao utilisateurDao;
    private final CommerceDao commerceDao;
    private final ReservationDao reservationDao;

    public UtilisateurServiceImpl() {
        this(new UtilisateurDaoImpl(), new CommerceDaoImpl(), new ReservationDaoImpl());
    }

    public UtilisateurServiceImpl(UtilisateurDao utilisateurDao, CommerceDao commerceDao,
                                  ReservationDao reservationDao) {
        this.utilisateurDao = utilisateurDao;
        this.commerceDao = commerceDao;
        this.reservationDao = reservationDao;
    }

    @Override
    public List<Utilisateur> listerTous() {
        return utilisateurDao.findAll();
    }

    @Override
    public Utilisateur trouverParId(Long id) {
        if (id == null) {
            throw new BusinessException(MSG_INTROUVABLE);
        }
        return utilisateurDao.findById(id).orElseThrow(() -> new BusinessException(MSG_INTROUVABLE));
    }

    @Override
    public Utilisateur creer(Utilisateur u, String motDePasse) {
        if (u.getRole() == Role.COMMERCANT) {
            throw new BusinessException("Un compte commerçant doit être créé avec son commerce");
        }
        return creerCompte(u, motDePasse, null);
    }

    @Override
    public Utilisateur creerCommercant(Utilisateur u, String motDePasse, Commerce commerce) {
        if (commerce == null) {
            throw new BusinessException("Le commerce est obligatoire pour un commerçant");
        }
        u.setRole(Role.COMMERCANT);
        return creerCompte(u, motDePasse, commerce);
    }

    @Override
    public Utilisateur modifier(Utilisateur donnees) {
        if (donnees == null || donnees.getId() == null) {
            throw new BusinessException(MSG_INTROUVABLE);
        }
        return JpaUtil.inTransaction(() -> {
            Utilisateur existant = utilisateurDao.findById(donnees.getId())
                    .orElseThrow(() -> new BusinessException(MSG_INTROUVABLE));
            String email = normaliser(donnees.getEmail());
            if (!email.equals(existant.getEmail()) && utilisateurDao.existsByEmail(email)) {
                throw new BusinessException(MSG_EMAIL_PRIS);
            }
            existant.setNom(texte(donnees.getNom()));
            existant.setPrenom(texte(donnees.getPrenom()));
            existant.setEmail(email);
            valider(existant);
            return utilisateurDao.save(existant);
        });
    }

    @Override
    public void changerRole(Long utilisateurId, Role nouveauRole) {
        JpaUtil.runInTransaction(() -> {
            Utilisateur utilisateur = utilisateurDao.findById(utilisateurId)
                    .orElseThrow(() -> new BusinessException(MSG_INTROUVABLE));
            if (nouveauRole == null) {
                throw new BusinessException("Rôle invalide");
            }
            Role ancienRole = utilisateur.getRole();
            if (ancienRole == nouveauRole) {
                return; // rien à changer
            }
            if (ancienRole == Role.ADMIN && utilisateurDao.findByRole(Role.ADMIN).size() <= 1) {
                throw new BusinessException("Impossible de retirer le rôle du dernier administrateur");
            }
            boolean aUnCommerce = commerceDao.findByProprietaire(utilisateurId).isPresent();
            if (ancienRole == Role.COMMERCANT && aUnCommerce) {
                throw new BusinessException("Cet utilisateur possède un commerce : son rôle ne peut pas être modifié");
            }
            if (nouveauRole == Role.COMMERCANT && !aUnCommerce) {
                throw new BusinessException("Un commerçant doit avoir un commerce : "
                        + "créez un compte commerçant avec son commerce");
            }
            utilisateur.setRole(nouveauRole);
            utilisateurDao.save(utilisateur);
        });
    }

    @Override
    public void supprimer(Long utilisateurId) {
        JpaUtil.runInTransaction(() -> {
            Utilisateur utilisateur = utilisateurDao.findById(utilisateurId)
                    .orElseThrow(() -> new BusinessException(MSG_INTROUVABLE));
            if (utilisateur.getRole() == Role.ADMIN && utilisateurDao.findByRole(Role.ADMIN).size() <= 1) {
                throw new BusinessException("Impossible de supprimer le dernier administrateur");
            }
            if (commerceDao.findByProprietaire(utilisateurId).isPresent()) {
                throw new BusinessException("Cet utilisateur possède un commerce : il ne peut pas être supprimé");
            }
            if (!reservationDao.findByUtilisateur(utilisateurId).isEmpty()) {
                throw new BusinessException("Cet utilisateur a des réservations : il ne peut pas être supprimé");
            }
            utilisateurDao.deleteById(utilisateurId);
        });
    }

    // ------------------------------------------------------------------ outils

    private Utilisateur creerCompte(Utilisateur source, String motDePasse, Commerce sourceCommerce) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            throw new BusinessException("Le mot de passe doit contenir au moins "
                    + LONGUEUR_MIN_MOT_DE_PASSE + " caractères");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(texte(source.getNom()));
        utilisateur.setPrenom(texte(source.getPrenom()));
        utilisateur.setEmail(normaliser(source.getEmail()));
        utilisateur.setRole(source.getRole());
        utilisateur.setMotDePasseHash(PasswordUtil.hash(motDePasse));
        valider(utilisateur);

        Commerce commerce = null;
        if (sourceCommerce != null) {
            commerce = new Commerce();
            commerce.setNom(texte(sourceCommerce.getNom()));
            commerce.setAdresse(texte(sourceCommerce.getAdresse()));
            commerce.setVille(texte(sourceCommerce.getVille()));
            commerce.setCategorie(texte(sourceCommerce.getCategorie()));
            String telephone = texte(sourceCommerce.getTelephone());
            commerce.setTelephone(telephone.isEmpty() ? null : telephone);
            commerce.setProprietaire(utilisateur); // pour la validation ; remplacé par l'utilisateur enregistré
            valider(commerce);
        }

        final Commerce nouveauCommerce = commerce;
        try {
            return JpaUtil.inTransaction(() -> {
                if (utilisateurDao.existsByEmail(utilisateur.getEmail())) {
                    throw new BusinessException(MSG_EMAIL_PRIS);
                }
                Utilisateur enregistre = utilisateurDao.save(utilisateur);
                if (nouveauCommerce != null) {
                    nouveauCommerce.setProprietaire(enregistre);
                    commerceDao.save(nouveauCommerce);
                }
                return enregistre;
            });
        } catch (PersistenceException e) {
            if (utilisateurDao.existsByEmail(utilisateur.getEmail())) {
                throw new BusinessException(MSG_EMAIL_PRIS);
            }
            throw new TechnicalException("Échec de la création du compte", e);
        }
    }

    private static <T> void valider(T objet) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(objet);
        if (!violations.isEmpty()) {
            throw new BusinessException(violations.iterator().next().getMessage());
        }
    }

    private static String normaliser(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }
}
