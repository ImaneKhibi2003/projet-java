package com.antigaspi.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T, ID> {
    T save(T entity);            // insert si id == null, sinon update
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
