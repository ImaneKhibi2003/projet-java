package com.antigaspi.security;

import com.antigaspi.model.Role;

/** Règles d'accès par URL (contrat d'interface, §8) et page d'accueil par rôle. */
public final class AccesUtil {

    private AccesUtil() {
    }

    /**
     * Rôle exigé pour accéder à ce chemin (sans le contexte de l'application),
     * ou null si le chemin est public.
     */
    public static Role roleRequis(String chemin) {
        if (correspond(chemin, "/client")) {
            return Role.CLIENT;
        }
        if (correspond(chemin, "/commercant")) {
            return Role.COMMERCANT;
        }
        if (correspond(chemin, "/admin")) {
            return Role.ADMIN;
        }
        return null;
    }

    /** Page d'arrivée après connexion. */
    public static String accueil(Role role) {
        return switch (role) {
            case CLIENT -> "/paniers";
            case COMMERCANT -> "/commercant/dashboard";
            case ADMIN -> "/admin/dashboard";
        };
    }

    private static boolean correspond(String chemin, String prefixe) {
        return chemin.equals(prefixe) || chemin.startsWith(prefixe + "/");
    }
}
