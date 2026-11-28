package com.antigaspi.service.impl;

import com.antigaspi.dao.CommerceDao;
import com.antigaspi.dao.impl.CommerceDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Commerce;
import com.antigaspi.service.CommerceService;

import java.util.List;

/**
 * VERSION PROVISOIRE écrite par A : seules les lectures sont implémentées (utilisées par le tableau de bord).
 * La personne B complète la gestion des commerces (tâche B3.1).
 */
public class CommerceServiceImpl implements CommerceService {

    private final CommerceDao commerceDao;

    public CommerceServiceImpl() {
        this(new CommerceDaoImpl());
    }

    public CommerceServiceImpl(CommerceDao commerceDao) {
        this.commerceDao = commerceDao;
    }

    @Override
    public List<Commerce> listerTous() {
        return commerceDao.findAll();
    }

    @Override
    public Commerce trouverParId(Long id) {
        if (id == null) {
            throw new BusinessException("Commerce introuvable");
        }
        return commerceDao.findById(id).orElseThrow(() -> new BusinessException("Commerce introuvable"));
    }

    @Override
    public Commerce trouverParProprietaire(Long utilisateurId) {
        if (utilisateurId == null) {
            throw new BusinessException("Aucun commerce n'est associé à votre compte");
        }
        return commerceDao.findByProprietaire(utilisateurId)
                .orElseThrow(() -> new BusinessException("Aucun commerce n'est associé à votre compte"));
    }

    @Override
    public Commerce creer(Commerce c) {
        throw new UnsupportedOperationException("À implémenter (gestion des commerces)");
    }

    @Override
    public Commerce modifier(Commerce c) {
        throw new UnsupportedOperationException("À implémenter (gestion des commerces)");
    }

    @Override
    public void supprimer(Long id) {
        throw new UnsupportedOperationException("À implémenter (gestion des commerces)");
    }
}
