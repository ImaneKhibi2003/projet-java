package com.antigaspi.service;

import com.antigaspi.model.Retrait;

import java.util.List;

public interface RetraitService {

    /**
     * Valide la remise d'un panier : vérifie le code, que la réservation est RESERVEE et que le panier
     * appartient au commerce du commerçant, crée le Retrait et passe la réservation à RETIREE.
     */
    Retrait validerRetrait(Long commercantId, String codeRetrait);

    /** Retraits validés aujourd'hui dans le commerce du commerçant (plus récents d'abord). */
    List<Retrait> listerRetraitsDuJour(Long commercantId);
}
