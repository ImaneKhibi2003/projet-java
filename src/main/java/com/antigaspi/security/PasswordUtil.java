package com.antigaspi.security;

import org.mindrot.jbcrypt.BCrypt;

/** Hachage et vérification des mots de passe (BCrypt, coût 10). */
public final class PasswordUtil {

    private static final int COUT = 10;

    private PasswordUtil() {
    }

    public static String hash(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt(COUT));
    }

    public static boolean verify(String motDePasse, String hash) {
        if (motDePasse == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(motDePasse, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash mal formé
        }
    }
}
