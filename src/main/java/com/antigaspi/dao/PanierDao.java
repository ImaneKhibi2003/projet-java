package com.antigaspi.dao;

import com.antigaspi.model.Panier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PanierDao extends CrudDao<Panier, Long> {

    /**
     * Paniers DISPONIBLE, quantiteRestante > 0, dateFinRetrait dans le futur.
     * categorie et prixMax peuvent être null (le filtre est alors ignoré).
     */
    List<Panier> findDisponibles(String categorie, BigDecimal prixMax);

    List<Panier> findByCommerce(Long commerceId);

    List<String> findCategories();

    /**
     * Charge le panier avec un verrou PESSIMISTIC_WRITE.
     * À appeler dans JpaUtil.inTransaction(...) : le verrou dure jusqu'à la fin de la transaction.
     */
    Optional<Panier> findByIdForUpdate(Long id);
}
