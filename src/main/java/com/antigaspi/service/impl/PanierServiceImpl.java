package com.antigaspi.service.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.PanierDao;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.model.Panier;
import com.antigaspi.model.StatutPanier;
import com.antigaspi.service.PanierService;
import jakarta.validation.ConstraintViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * VERSION PROVISOIRE écrite par A pour débloquer les servlets (A4.2, A4.3, A4.7).
 * La personne B la complète (tâche B3.1) en gardant les signatures de PanierService.
 */
public class PanierServiceImpl implements PanierService {

    private static final String MSG_PAS_PROPRIETAIRE = "Vous n'êtes pas propriétaire de ce panier";

    private final PanierDao panierDao;
    private final CommerceDao commerceDao;
    private final ReservationDao reservationDao;

    public PanierServiceImpl() {
        this(new PanierDaoImpl(), new CommerceDaoImpl(), new ReservationDaoImpl());
    }

    public PanierServiceImpl(PanierDao panierDao, CommerceDao commerceDao, ReservationDao reservationDao) {
        this.panierDao = panierDao;
        this.commerceDao = commerceDao;
        this.reservationDao = reservationDao;
    }

    // ------------------------------------------------------------- consultation

    @Override
    public List<Panier> rechercher(String categorie, BigDecimal prixMax, String tri) {
        List<Panier> paniers = new ArrayList<>(panierDao.findDisponibles(categorie, prixMax));
        if ("prix".equals(tri)) {
            paniers.sort(Comparator.comparing(Panier::getPrixReduit)
                    .thenComparing(Panier::getDateFinRetrait));
        } else {
            paniers.sort(Comparator.comparing(Panier::getDateFinRetrait)
                    .thenComparing(Panier::getId));
        }
        return paniers;
    }

    @Override
    public Panier trouverParId(Long id) {
        if (id == null) {
            throw new BusinessException("Panier introuvable");
        }
        return panierDao.findById(id).orElseThrow(() -> new BusinessException("Panier introuvable"));
    }

    @Override
    public List<Panier> listerParCommerce(Long commerceId) {
        return panierDao.findByCommerce(commerceId);
    }

    @Override
    public List<String> listerCategories() {
        return panierDao.findCategories();
    }

    // ------------------------------------------------------------------- CRUD

    @Override
    public Panier creer(Panier p, Long commercantId) {
        verifierRegles(p);
        try {
            return JpaUtil.inTransaction(() -> {
                Commerce commerce = commerceDao.findByProprietaire(commercantId)
                        .orElseThrow(() -> new BusinessException("Aucun commerce n'est associé à votre compte"));
                Panier nouveau = new Panier();
                copierChampsModifiables(p, nouveau);
                nouveau.setCommerce(commerce);
                nouveau.setQuantiteRestante(p.getQuantiteTotale());
                nouveau.setStatut(StatutPanier.DISPONIBLE);
                return panierDao.save(nouveau);
            });
        } catch (ConstraintViolationException e) {
            throw new BusinessException(e.getConstraintViolations().iterator().next().getMessage());
        }
    }

    @Override
    public Panier modifier(Panier donnees, Long commercantId) {
        if (donnees == null || donnees.getId() == null) {
            throw new BusinessException("Panier introuvable");
        }
        verifierRegles(donnees);
        try {
            return JpaUtil.inTransaction(() -> {
                Panier existant = panierDao.findByIdForUpdate(donnees.getId())
                        .orElseThrow(() -> new BusinessException("Panier introuvable"));
                verifierProprietaire(existant, commercantId);

                // paniers déjà pris par des clients (réservations en cours ou retirées)
                int reserves = existant.getQuantiteTotale() - existant.getQuantiteRestante();
                if (donnees.getQuantiteTotale() < reserves) {
                    throw new BusinessException("La quantité totale ne peut pas être inférieure "
                            + "aux paniers déjà réservés (" + reserves + ")");
                }

                copierChampsModifiables(donnees, existant);
                existant.setQuantiteRestante(donnees.getQuantiteTotale() - reserves);
                // la date de fin est forcément future (vérifiée plus haut) : un panier expiré est donc remis en vente
                existant.setStatut(existant.getQuantiteRestante() == 0 ? StatutPanier.EPUISE : StatutPanier.DISPONIBLE);
                return panierDao.save(existant);
            });
        } catch (ConstraintViolationException e) {
            throw new BusinessException(e.getConstraintViolations().iterator().next().getMessage());
        }
    }

    @Override
    public void supprimer(Long panierId, Long commercantId) {
        JpaUtil.runInTransaction(() -> {
            Panier panier = panierDao.findByIdForUpdate(panierId)
                    .orElseThrow(() -> new BusinessException("Panier introuvable"));
            verifierProprietaire(panier, commercantId);

            boolean aDesReservations = reservationDao.findByCommerce(panier.getCommerce().getId()).stream()
                    .anyMatch(r -> r.getPanier().getId().equals(panierId));
            if (aDesReservations) {
                throw new BusinessException("Ce panier a déjà des réservations : il ne peut plus être supprimé");
            }
            panierDao.deleteById(panierId);
        });
    }

    // ------------------------------------------------------------------ outils

    private static void verifierProprietaire(Panier panier, Long commercantId) {
        Commerce commerce = panier.getCommerce();
        if (commercantId == null || commerce.getProprietaire() == null
                || !commerce.getProprietaire().getId().equals(commercantId)) {
            throw new BusinessException(MSG_PAS_PROPRIETAIRE);
        }
    }

    private static void copierChampsModifiables(Panier source, Panier cible) {
        cible.setTitre(source.getTitre().trim());
        cible.setDescription(source.getDescription() == null || source.getDescription().isBlank()
                ? null : source.getDescription().trim());
        cible.setCategorie(source.getCategorie().trim());
        cible.setPrixInitial(source.getPrixInitial());
        cible.setPrixReduit(source.getPrixReduit());
        cible.setQuantiteTotale(source.getQuantiteTotale());
        cible.setDateDebutRetrait(source.getDateDebutRetrait());
        cible.setDateFinRetrait(source.getDateFinRetrait());
    }

    /** Règles métier de création et de modification d'un panier. */
    private static void verifierRegles(Panier p) {
        if (p == null || p.getTitre() == null || p.getTitre().isBlank()) {
            throw new BusinessException("Le titre est obligatoire");
        }
        if (p.getCategorie() == null || p.getCategorie().isBlank()) {
            throw new BusinessException("La catégorie est obligatoire");
        }
        if (p.getQuantiteTotale() < 1) {
            throw new BusinessException("La quantité doit être au moins 1");
        }
        if (p.getPrixInitial() == null || p.getPrixReduit() == null
                || p.getPrixInitial().signum() <= 0 || p.getPrixReduit().signum() < 0) {
            throw new BusinessException("Les prix doivent être positifs");
        }
        if (p.getPrixReduit().compareTo(p.getPrixInitial()) > 0) {
            throw new BusinessException("Le prix réduit doit être inférieur ou égal au prix initial");
        }
        if (p.getDateDebutRetrait() == null || p.getDateFinRetrait() == null) {
            throw new BusinessException("Les dates de retrait sont obligatoires");
        }
        if (!p.getDateFinRetrait().isAfter(p.getDateDebutRetrait())) {
            throw new BusinessException("La date de fin doit être après la date de début");
        }
        if (!p.getDateFinRetrait().isAfter(LocalDateTime.now())) {
            throw new BusinessException("La date de fin doit être dans le futur");
        }
    }
}
