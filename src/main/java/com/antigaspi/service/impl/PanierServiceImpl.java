package com.antigaspi.service.impl;

import com.antigaspi.dao.PanierDao;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.service.PanierService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * VERSION PROVISOIRE écrite par A pour débloquer les servlets (A4.2, A4.3).
 * La personne B la complète (tâche B3.1) en gardant les signatures de PanierService.
 */
public class PanierServiceImpl implements PanierService {

    private final PanierDao panierDao;

    public PanierServiceImpl() {
        this(new PanierDaoImpl());
    }

    public PanierServiceImpl(PanierDao panierDao) {
        this.panierDao = panierDao;
    }

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

    @Override
    public Panier creer(Panier p, Long commercantId) {
        throw new UnsupportedOperationException("À implémenter avec PanierCrudServlet (A4.7)");
    }

    @Override
    public Panier modifier(Panier p, Long commercantId) {
        throw new UnsupportedOperationException("À implémenter avec PanierCrudServlet (A4.7)");
    }

    @Override
    public void supprimer(Long panierId, Long commercantId) {
        throw new UnsupportedOperationException("À implémenter avec PanierCrudServlet (A4.7)");
    }
}
