package com.antigaspi.exception;

/** Erreur métier : le message est destiné à être affiché à l'utilisateur. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
