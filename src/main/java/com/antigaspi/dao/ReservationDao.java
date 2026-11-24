package com.antigaspi.dao;

import com.antigaspi.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationDao extends CrudDao<Reservation, Long> {
    List<Reservation> findByUtilisateur(Long utilisateurId);   // plus récentes d'abord
    List<Reservation> findByCommerce(Long commerceId);         // plus récentes d'abord
    Optional<Reservation> findByCodeRetrait(String code);
    boolean existsByCodeRetrait(String code);
    boolean existsActiveByUtilisateurAndPanier(Long utilisateurId, Long panierId);
    List<Reservation> findExpirees();                          // RESERVEE + fenêtre de retrait dépassée
}
