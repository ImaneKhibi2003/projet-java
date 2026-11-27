package com.antigaspi.service;

import com.antigaspi.model.Panier;

import java.math.BigDecimal;
import java.util.List;

public interface PanierService {

    /**
     * Paniers disponibles filtrés. categorie et prixMax peuvent être null.
     * tri : "prix" (prix réduit croissant) ou "date" (fin de retrait la plus proche d'abord, défaut).
     */
    List<Panier> rechercher(String categorie, BigDecimal prixMax, String tri);

    /** Lève BusinessException("Panier introuvable") si l'id est null ou inconnu. */
    Panier trouverParId(Long id);

    List<Panier> listerParCommerce(Long commerceId);

    List<String> listerCategories();

    // Les 3 méthodes ci-dessous vérifient que le commerçant est propriétaire (implémentées avec A4.7)
    Panier creer(Panier p, Long commercantId);

    Panier modifier(Panier p, Long commercantId);

    void supprimer(Long panierId, Long commercantId);
}
