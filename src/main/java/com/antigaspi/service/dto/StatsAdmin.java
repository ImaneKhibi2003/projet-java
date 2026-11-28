package com.antigaspi.service.dto;

import java.math.BigDecimal;

/** Statistiques globales de la plateforme (voir la remarque sur StatsCommercant : classe à getters). */
public class StatsAdmin {

    private final int nbUtilisateurs;
    private final int nbCommerces;
    private final int nbPaniers;
    private final int nbReservations;
    private final int nbRetraits;
    private final BigDecimal chiffreAffairesTotal;
    private final int invendusSauves;

    public StatsAdmin(int nbUtilisateurs, int nbCommerces, int nbPaniers, int nbReservations,
                      int nbRetraits, BigDecimal chiffreAffairesTotal, int invendusSauves) {
        this.nbUtilisateurs = nbUtilisateurs;
        this.nbCommerces = nbCommerces;
        this.nbPaniers = nbPaniers;
        this.nbReservations = nbReservations;
        this.nbRetraits = nbRetraits;
        this.chiffreAffairesTotal = chiffreAffairesTotal;
        this.invendusSauves = invendusSauves;
    }

    public int getNbUtilisateurs() {
        return nbUtilisateurs;
    }

    public int getNbCommerces() {
        return nbCommerces;
    }

    public int getNbPaniers() {
        return nbPaniers;
    }

    public int getNbReservations() {
        return nbReservations;
    }

    public int getNbRetraits() {
        return nbRetraits;
    }

    public BigDecimal getChiffreAffairesTotal() {
        return chiffreAffairesTotal;
    }

    public int getInvendusSauves() {
        return invendusSauves;
    }
}
