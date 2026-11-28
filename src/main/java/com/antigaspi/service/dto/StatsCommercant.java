package com.antigaspi.service.dto;

import java.math.BigDecimal;

/**
 * Statistiques d'un commerce. Classe à getters (et non record) : EL 5.0 de Tomcat 10.1
 * ne sait pas lire les records dans les JSP (${stats.nbPaniers}).
 */
public class StatsCommercant {

    private final int nbPaniers;
    private final int nbReservations;
    private final BigDecimal chiffreAffaires;
    private final int invendusSauves;

    public StatsCommercant(int nbPaniers, int nbReservations, BigDecimal chiffreAffaires, int invendusSauves) {
        this.nbPaniers = nbPaniers;
        this.nbReservations = nbReservations;
        this.chiffreAffaires = chiffreAffaires;
        this.invendusSauves = invendusSauves;
    }

    /** Paniers publiés par le commerce (tous statuts). */
    public int getNbPaniers() {
        return nbPaniers;
    }

    /** Réservations reçues (tous statuts). */
    public int getNbReservations() {
        return nbReservations;
    }

    /** Somme de prixReduit x quantité des réservations RETIREE. */
    public BigDecimal getChiffreAffaires() {
        return chiffreAffaires;
    }

    /** Somme des quantités des réservations RETIREE. */
    public int getInvendusSauves() {
        return invendusSauves;
    }
}
