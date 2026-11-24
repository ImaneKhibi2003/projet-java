import org.mindrot.jbcrypt.BCrypt;

/**
 * A2.4 : génère les hashes BCrypt des comptes de test.
 * Dépendance Maven : org.mindrot:jbcrypt:0.4
 * Lancer le main, puis copier les hashes dans data.sql.
 */
public class HashGenerator {

    public static void main(String[] args) {
        String motDePasse = "password123";

        String[] comptes = {
            "admin@antigaspi.ma",
            "boulangerie@antigaspi.ma",
            "restaurant@antigaspi.ma",
            "primeur@antigaspi.ma",
            "sara@antigaspi.ma",
            "karim@antigaspi.ma"
        };

        for (String compte : comptes) {
            // gensalt(10) : coût 10, un sel aléatoire différent à chaque appel
            String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt(10));
            System.out.println(compte + " -> " + hash);
        }

        // Vérification : checkpw compare le mot de passe en clair au hash
        String test = BCrypt.hashpw(motDePasse, BCrypt.gensalt(10));
        System.out.println("Vérification : " + BCrypt.checkpw(motDePasse, test));
    }
}
