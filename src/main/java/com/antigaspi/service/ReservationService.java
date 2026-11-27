package com.antigaspi.service;

import com.antigaspi.model.Reservation;

import java.util.List;

public interface ReservationService {

    /**
     * Réserve des paniers pour un client. Transaction avec verrou sur le panier :
     * vérifie statut, date, quantité, anti-doublon, décrémente le stock, passe le panier
     * à EPUISE s'il ne reste rien et génère le code de retrait.
     */
    Reservation reserver(Long clientId, Long panierId, int quantite);

    /** Annule une réservation RESERVEE du client, rend les paniers au stock. */
    void annuler(Long clientId, Long reservationId);

    List<Reservation> listerParClient(Long clientId);

    List<Reservation> listerParCommerce(Long commerceId);
}
