package com.antigaspi.exception;

/** Erreur technique (base de données, configuration...) : à journaliser, pas à afficher telle quelle. */
public class TechnicalException extends RuntimeException {

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
