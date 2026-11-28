package com.antigaspi.service;

import com.antigaspi.model.Commerce;

import java.util.List;

public interface CommerceService {

    List<Commerce> listerTous();

    /** Lève BusinessException("Commerce introuvable"). */
    Commerce trouverParId(Long id);

    /** Lève BusinessException("Aucun commerce n'est associé à votre compte"). */
    Commerce trouverParProprietaire(Long utilisateurId);

    Commerce creer(Commerce c);

    Commerce modifier(Commerce c);

    void supprimer(Long id);
}
