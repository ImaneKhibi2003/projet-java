package com.antigaspi.web.commercant;

import com.antigaspi.model.Panier;
import com.antigaspi.model.StatutPanier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formulaire de création/modification d'un panier.
 * Garde les valeurs saisies sous forme de texte (pour réafficher le formulaire) et produit
 * une erreur par champ : Map champ -> message (attribut "erreurs" des JSP).
 */
public class PanierFormulaire {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private String id = "";
    private String titre = "";
    private String description = "";
    private String categorie = "";
    private String prixInitial = "";
    private String prixReduit = "";
    private String quantiteTotale = "";
    private String dateDebutRetrait = "";
    private String dateFinRetrait = "";

    private final Map<String, String> erreurs = new LinkedHashMap<>();
    private Panier panier;

    /** Formulaire vide avec des valeurs par défaut : retrait d'aujourd'hui, dans l'heure qui vient jusqu'à +4 h. */
    public static PanierFormulaire vide() {
        PanierFormulaire f = new PanierFormulaire();
        LocalDateTime maintenant = LocalDateTime.now().withSecond(0).withNano(0);
        f.quantiteTotale = "1";
        f.dateDebutRetrait = maintenant.format(FORMAT_DATE);
        f.dateFinRetrait = maintenant.plusHours(4).format(FORMAT_DATE);
        return f;
    }

    /** Formulaire prérempli avec un panier existant. */
    public static PanierFormulaire depuis(Panier p) {
        PanierFormulaire f = new PanierFormulaire();
        f.id = String.valueOf(p.getId());
        f.titre = p.getTitre();
        f.description = p.getDescription() == null ? "" : p.getDescription();
        f.categorie = p.getCategorie();
        f.prixInitial = p.getPrixInitial().toPlainString();
        f.prixReduit = p.getPrixReduit().toPlainString();
        f.quantiteTotale = String.valueOf(p.getQuantiteTotale());
        f.dateDebutRetrait = p.getDateDebutRetrait().format(FORMAT_DATE);
        f.dateFinRetrait = p.getDateFinRetrait().format(FORMAT_DATE);
        return f;
    }

    /** Formulaire rempli avec les paramètres de la requête. */
    public static PanierFormulaire lire(HttpServletRequest req) {
        PanierFormulaire f = new PanierFormulaire();
        f.id = texte(req.getParameter("id"));
        f.titre = texte(req.getParameter("titre"));
        f.description = texte(req.getParameter("description"));
        f.categorie = texte(req.getParameter("categorie"));
        f.prixInitial = texte(req.getParameter("prixInitial"));
        f.prixReduit = texte(req.getParameter("prixReduit"));
        f.quantiteTotale = texte(req.getParameter("quantiteTotale"));
        f.dateDebutRetrait = texte(req.getParameter("dateDebutRetrait"));
        f.dateFinRetrait = texte(req.getParameter("dateFinRetrait"));
        return f;
    }

    /** Contrôle les champs. Retourne true si tout est valide ; le Panier est alors disponible via getPanier(). */
    public boolean valider() {
        erreurs.clear();
        Panier p = new Panier();

        if (!id.isEmpty()) {
            try {
                p.setId(Long.valueOf(id));
            } catch (NumberFormatException e) {
                erreurs.put("id", "Identifiant invalide");
            }
        }
        p.setTitre(titre);
        p.setDescription(description.isEmpty() ? null : description);
        p.setCategorie(categorie);
        p.setPrixInitial(lireDecimal("prixInitial", prixInitial));
        p.setPrixReduit(lireDecimal("prixReduit", prixReduit));
        p.setQuantiteTotale(lireEntier("quantiteTotale", quantiteTotale));
        p.setDateDebutRetrait(lireDate("dateDebutRetrait", dateDebutRetrait));
        p.setDateFinRetrait(lireDate("dateFinRetrait", dateFinRetrait));
        // valeurs techniques calculées par le service : renseignées ici seulement pour la validation
        p.setQuantiteRestante(p.getQuantiteTotale());
        p.setStatut(StatutPanier.DISPONIBLE);

        for (ConstraintViolation<Panier> v : VALIDATOR.validate(p)) {
            String champ = champDe(v.getPropertyPath().toString());
            if (champ != null) {
                erreurs.putIfAbsent(champ, v.getMessage()); // l'erreur de format éventuelle reste prioritaire
            }
        }
        if (p.getDateFinRetrait() != null && !p.getDateFinRetrait().isAfter(LocalDateTime.now())) {
            erreurs.putIfAbsent("dateFinRetrait", "La date de fin doit être dans le futur");
        }

        panier = p;
        return erreurs.isEmpty();
    }

    // ------------------------------------------------------------------ outils

    /** Champ du formulaire concerné par une violation (null = à ignorer). */
    private static String champDe(String propriete) {
        return switch (propriete) {
            case "prixCoherent" -> "prixReduit";
            case "datesCoherentes" -> "dateFinRetrait";
            case "quantitesCoherentes" -> "quantiteTotale";
            case "commerce", "statut", "quantiteRestante" -> null; // renseignés par le service
            default -> propriete;
        };
    }

    private BigDecimal lireDecimal(String champ, String valeur) {
        if (valeur.isEmpty()) {
            return null; // le message "obligatoire" vient de Bean Validation
        }
        try {
            return new BigDecimal(valeur.replace(',', '.'));
        } catch (NumberFormatException e) {
            erreurs.put(champ, "Nombre invalide");
            return null;
        }
    }

    private int lireEntier(String champ, String valeur) {
        if (valeur.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valeur);
        } catch (NumberFormatException e) {
            erreurs.put(champ, "Nombre entier invalide");
            return 0;
        }
    }

    private LocalDateTime lireDate(String champ, String valeur) {
        if (valeur.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valeur);
        } catch (DateTimeParseException e) {
            erreurs.put(champ, "Date invalide");
            return null;
        }
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }

    // ---------------------------------------------------------- accesseurs (JSP)

    public String getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getPrixInitial() {
        return prixInitial;
    }

    public String getPrixReduit() {
        return prixReduit;
    }

    public String getQuantiteTotale() {
        return quantiteTotale;
    }

    public String getDateDebutRetrait() {
        return dateDebutRetrait;
    }

    public String getDateFinRetrait() {
        return dateFinRetrait;
    }

    public Map<String, String> getErreurs() {
        return erreurs;
    }

    public Panier getPanier() {
        return panier;
    }
}
