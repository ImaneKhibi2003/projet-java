package com.antigaspi.service;

import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.exception.TechnicalException;

import java.security.SecureRandom;

/**
 * Génère les codes de retrait : 6 caractères, sans I, O, 0 et 1 (faciles à confondre à l'oral
 * ou à l'écrit). L'unicité est vérifiée en base.
 */
public class CodeGenerator {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LONGUEUR = 6;
    private static final int TENTATIVES_MAX = 20;

    private final SecureRandom hasard = new SecureRandom();
    private final ReservationDao reservationDao;

    public CodeGenerator() {
        this(new ReservationDaoImpl());
    }

    public CodeGenerator(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public String generer() {
        for (int i = 0; i < TENTATIVES_MAX; i++) {
            String code = codeAleatoire();
            if (!reservationDao.existsByCodeRetrait(code)) {
                return code;
            }
        }
        throw new TechnicalException("Impossible de générer un code de retrait unique", null);
    }

    private String codeAleatoire() {
        StringBuilder sb = new StringBuilder(LONGUEUR);
        for (int i = 0; i < LONGUEUR; i++) {
            sb.append(ALPHABET.charAt(hasard.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
