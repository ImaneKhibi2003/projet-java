package com.antigaspi.dao;

import com.antigaspi.model.Retrait;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RetraitDao extends CrudDao<Retrait, Long> {
    Optional<Retrait> findByReservation(Long reservationId);
    List<Retrait> findByCommerceAndDate(Long commerceId, LocalDate jour);
}
