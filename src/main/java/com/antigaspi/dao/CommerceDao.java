package com.antigaspi.dao;

import com.antigaspi.model.Commerce;

import java.util.Optional;

public interface CommerceDao extends CrudDao<Commerce, Long> {
    Optional<Commerce> findByProprietaire(Long utilisateurId);
}
