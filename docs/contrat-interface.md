# Contrat d'interface — Anti-gaspillage alimentaire

> Document partagé entre **A** (données, DAO, servlets) et **B** (services, sécurité, JSP).
> Toute modification passe par une PR relue par l'autre personne.
> **Version 1.0** — validé par A : ☐ oui  /  validé par B : ☐ oui

---

## 1. Conventions générales

| Élément | Convention |
|---|---|
| Langage / Jakarta | Java 17, Jakarta EE 10 |
| Package racine | `com.antigaspi` |
| Packages | `model`, `dao`, `service`, `security`, `exception`, `filter`, `web.*` |
| Dates | `java.time.LocalDateTime` |
| Montants | `java.math.BigDecimal` (jamais `double`) |
| Identifiants | `Long` |
| Enums en base | `@Enumerated(EnumType.STRING)` |
| Nommage | Classes en français sans accent, attributs en camelCase, colonnes SQL en snake_case |
| Gestion d'erreurs | Les services lèvent `BusinessException` (erreur métier, message affichable) ou `TechnicalException` (erreur technique) |

---

## 2. Énumérations (package `model`)

```java
public enum Role { CLIENT, COMMERCANT, ADMIN }
public enum StatutPanier { DISPONIBLE, EPUISE, EXPIRE }
public enum StatutReservation { RESERVEE, RETIREE, ANNULEE, EXPIREE }
```

**Transitions autorisées**

| Entité | De | Vers | Déclencheur |
|---|---|---|---|
| Panier | DISPONIBLE | EPUISE | `quantiteRestante` atteint 0 |
| Panier | EPUISE | DISPONIBLE | annulation / expiration d'une réservation |
| Panier | DISPONIBLE | EXPIRE | `dateFinRetrait` dépassée |
| Reservation | RESERVEE | RETIREE | `RetraitService.validerRetrait()` |
| Reservation | RESERVEE | ANNULEE | `ReservationService.annuler()` |
| Reservation | RESERVEE | EXPIREE | `ExpirationJob` |

Les autres transitions sont interdites (ex. RETIREE → ANNULEE).

---

## 3. Entités (package `model`)

Toutes les entités ont getters/setters, un constructeur sans argument, et `equals/hashCode` sur `id`.

### Utilisateur — table `utilisateur`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK, auto |
| `nom` | String | `@NotBlank`, max 100 |
| `prenom` | String | `@NotBlank`, max 100 |
| `email` | String | `@NotBlank` `@Email`, unique |
| `motDePasseHash` | String | hash BCrypt, jamais le mot de passe en clair |
| `role` | Role | `@NotNull` |
| `dateInscription` | LocalDateTime | auto à la création |
| `version` | Long | `@Version` |

### Commerce — table `commerce`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `nom` | String | `@NotBlank` |
| `adresse` | String | `@NotBlank` |
| `ville` | String | `@NotBlank` |
| `telephone` | String | optionnel |
| `categorie` | String | `@NotBlank` |
| `proprietaire` | Utilisateur | `@OneToOne`, obligatoire, rôle COMMERCANT |

### Panier — table `panier`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `commerce` | Commerce | `@ManyToOne`, obligatoire |
| `titre` | String | `@NotBlank` |
| `description` | String | optionnel, max 500 |
| `categorie` | String | `@NotBlank` |
| `prixInitial` | BigDecimal | `@Positive` |
| `prixReduit` | BigDecimal | `@PositiveOrZero`, ≤ `prixInitial` |
| `quantiteTotale` | int | `@Min(1)` |
| `quantiteRestante` | int | `@Min(0)`, ≤ `quantiteTotale` |
| `dateDebutRetrait` | LocalDateTime | `@NotNull` |
| `dateFinRetrait` | LocalDateTime | `@NotNull` `@Future` à la création, > `dateDebutRetrait` |
| `statut` | StatutPanier | défaut DISPONIBLE |
| `version` | Long | `@Version` |

### Reservation — table `reservation`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `panier` | Panier | `@ManyToOne`, obligatoire |
| `utilisateur` | Utilisateur | `@ManyToOne`, le client, obligatoire |
| `quantite` | int | `@Min(1)` |
| `codeRetrait` | String | 6 caractères alphanumériques majuscules, unique |
| `dateReservation` | LocalDateTime | auto |
| `statut` | StatutReservation | défaut RESERVEE |
| `version` | Long | `@Version` |

### Retrait — table `retrait`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `reservation` | Reservation | `@OneToOne`, obligatoire, unique |
| `dateRetrait` | LocalDateTime | auto |

### Evaluation — table `evaluation`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `reservation` | Reservation | `@OneToOne`, obligatoire, unique |
| `note` | int | `@Min(1)` `@Max(5)` |
| `commentaire` | String | optionnel, max 500 |
| `dateEvaluation` | LocalDateTime | auto |

---

## 4. DAO (package `dao`) — responsable : A

Chaque DAO expose le CRUD générique :

```java
public interface CrudDao<T, ID> {
    T save(T entity);                 // insert ou update
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

Méthodes spécifiques :

```java
public interface UtilisateurDao extends CrudDao<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(Role role);
}

public interface CommerceDao extends CrudDao<Commerce, Long> {
    Optional<Commerce> findByProprietaire(Long utilisateurId);
}

public interface PanierDao extends CrudDao<Panier, Long> {
    // Paniers DISPONIBLE, quantiteRestante > 0, dateFinRetrait > maintenant.
    // categorie et prixMax peuvent être null (filtre ignoré).
    List<Panier> findDisponibles(String categorie, BigDecimal prixMax);
    List<Panier> findByCommerce(Long commerceId);
    List<String> findCategories();
    // Verrou PESSIMISTIC_WRITE, à appeler dans une transaction du service
    Optional<Panier> findByIdForUpdate(Long id);
}

public interface ReservationDao extends CrudDao<Reservation, Long> {
    List<Reservation> findByUtilisateur(Long utilisateurId);   // tri : plus récentes d'abord
    List<Reservation> findByCommerce(Long commerceId);
    Optional<Reservation> findByCodeRetrait(String code);
    boolean existsByCodeRetrait(String code);
    // Réservation RESERVEE du même client sur le même panier (anti-doublon)
    boolean existsActiveByUtilisateurAndPanier(Long utilisateurId, Long panierId);
    // statut RESERVEE et panier.dateFinRetrait < maintenant
    List<Reservation> findExpirees();
}

public interface RetraitDao extends CrudDao<Retrait, Long> {
    Optional<Retrait> findByReservation(Long reservationId);
    List<Retrait> findByCommerceAndDate(Long commerceId, LocalDate jour);
}
```

Les DAO ne gèrent **pas** les transactions : elles sont ouvertes par la couche service.

---

## 5. Services (package `service`) — responsable : B

```java
public interface AuthService {
    // Lève BusinessException("Email ou mot de passe incorrect")
    Utilisateur login(String email, String motDePasse);
    // Rôle CLIENT par défaut. Lève BusinessException si email déjà utilisé.
    Utilisateur register(String nom, String prenom, String email, String motDePasse);
}

public interface UtilisateurService {
    List<Utilisateur> listerTous();
    Utilisateur trouverParId(Long id);
    Utilisateur creer(Utilisateur u, String motDePasse);
    Utilisateur modifier(Utilisateur u);
    void changerRole(Long utilisateurId, Role nouveauRole);
    void supprimer(Long utilisateurId);
}

public interface CommerceService {
    List<Commerce> listerTous();
    Commerce trouverParId(Long id);
    Commerce trouverParProprietaire(Long utilisateurId);
    Commerce creer(Commerce c);
    Commerce modifier(Commerce c);
    void supprimer(Long id);
}

public interface PanierService {
    List<Panier> rechercher(String categorie, BigDecimal prixMax, String tri); // tri : "prix" | "date" | null
    Panier trouverParId(Long id);
    List<Panier> listerParCommerce(Long commerceId);
    List<String> listerCategories();
    // Les 3 méthodes ci-dessous vérifient que le commerçant est propriétaire
    Panier creer(Panier p, Long commercantId);
    Panier modifier(Panier p, Long commercantId);
    void supprimer(Long panierId, Long commercantId);
}

public interface ReservationService {
    Reservation reserver(Long clientId, Long panierId, int quantite);
    void annuler(Long clientId, Long reservationId);
    List<Reservation> listerParClient(Long clientId);
    List<Reservation> listerParCommerce(Long commerceId);
}

public interface RetraitService {
    Retrait validerRetrait(Long commercantId, String codeRetrait);
    List<Retrait> listerRetraitsDuJour(Long commercantId);
}

public interface StatsService {
    StatsCommercant statsCommercant(Long commercantId);
    StatsAdmin statsAdmin();
}
```

### DTO de statistiques (package `service.dto`)

```java
public record StatsCommercant(int nbPaniers, int nbReservations,
                              BigDecimal chiffreAffaires, int invendusSauves) {}

public record StatsAdmin(int nbUtilisateurs, int nbCommerces, int nbPaniers,
                         int nbReservations, int nbRetraits,
                         BigDecimal chiffreAffairesTotal, int invendusSauves) {}
```

**Définitions** : `invendusSauves` = somme des `quantite` des réservations RETIREE ; `chiffreAffaires` = somme de `prixReduit × quantite` des réservations RETIREE.

---

## 6. Règles métier et messages d'erreur

Les messages `BusinessException` ci-dessous sont **exacts** (affichés tels quels dans les JSP).

| Opération | Condition d'échec | Message |
|---|---|---|
| `reserver` | Panier non DISPONIBLE | `Ce panier n'est plus disponible` |
| `reserver` | `dateFinRetrait` dépassée | `Ce panier est expiré` |
| `reserver` | `quantite` > `quantiteRestante` | `Quantité insuffisante : il reste X panier(s)` |
| `reserver` | `quantite` < 1 | `La quantité doit être au moins 1` |
| `reserver` | Doublon actif | `Vous avez déjà une réservation en cours pour ce panier` |
| `reserver` | Utilisateur pas CLIENT | `Seuls les clients peuvent réserver` |
| `annuler` | Réservation d'un autre client | `Cette réservation ne vous appartient pas` |
| `annuler` | Statut ≠ RESERVEE | `Seule une réservation en cours peut être annulée` |
| `validerRetrait` | Code inconnu | `Code de retrait invalide` |
| `validerRetrait` | Statut ≠ RESERVEE | `Cette réservation n'est plus valide (statut : X)` |
| `validerRetrait` | Autre commerce | `Ce code ne correspond à aucun panier de votre commerce` |
| `register` | Email existant | `Cet email est déjà utilisé` |
| `login` | Mauvais identifiants | `Email ou mot de passe incorrect` |
| `panier.*` | Commerçant non propriétaire | `Vous n'êtes pas propriétaire de ce panier` |

**Code de retrait** : `CodeGenerator.generer()` retourne 6 caractères parmi `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (sans `I`, `O`, `0`, `1` pour éviter les confusions), unicité vérifiée via `ReservationDao.existsByCodeRetrait`.

---

## 7. Exceptions (package `exception`)

```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
public class TechnicalException extends RuntimeException {
    public TechnicalException(String message, Throwable cause) { super(message, cause); }
}
```

- `BusinessException` : le servlet affiche le message dans un flash d'erreur.
- `TechnicalException` : le servlet journalise (`java.util.logging`) et redirige vers `error.jsp` (500).

---

## 8. Session, flash messages et sécurité

| Élément | Contrat |
|---|---|
| Utilisateur connecté | `session.setAttribute("utilisateur", utilisateur)` → dans les JSP : `${sessionScope.utilisateur}` |
| Rôle dans les JSP | `${sessionScope.utilisateur.role == 'ADMIN'}` |
| Flash succès | attribut `flashSucces` (String) |
| Flash erreur | attribut `flashErreur` (String) |
| Mécanisme flash | `FlashUtil.succes(request, msg)` / `FlashUtil.erreur(request, msg)` stockent en session ; un filtre les place dans la requête puis les supprime de la session. `messages.jsp` lit `${flashSucces}` et `${flashErreur}` |
| Hash mot de passe | `PasswordUtil.hash(String)` et `PasswordUtil.verify(String, String)` (BCrypt, coût 10) |

**Zones protégées par `AuthFilter`**

| URL | Accès |
|---|---|
| `/login`, `/register`, `/paniers`, `/panier`, `/css/*` | Public |
| `/client/*` | CLIENT |
| `/commercant/*` | COMMERCANT |
| `/admin/*` | ADMIN |

Non connecté sur zone protégée : redirection vers `/login`. Mauvais rôle : erreur **403**.

---

## 9. Routes web : servlets (A) ↔ JSP (B)

JSP placées sous `WEB-INF/views/`. Les attributs de requête listés sont ceux que le servlet **doit** fournir.

| URL | Méthode | Servlet (A) | JSP (B) | Paramètres reçus | Attributs fournis à la JSP |
|---|---|---|---|---|---|
| `/login` | GET/POST | `LoginServlet` | `auth/login.jsp` | `email`, `motDePasse` | — |
| `/register` | GET/POST | `RegisterServlet` | `auth/register.jsp` | `nom`, `prenom`, `email`, `motDePasse` | — |
| `/logout` | GET | `LogoutServlet` | — | — | — |
| `/paniers` | GET | `PanierListServlet` | `panier/list.jsp` | `categorie`, `prixMax`, `tri`, `page` | `paniers`, `categories`, `page`, `nbPages` |
| `/panier` | GET | `PanierDetailServlet` | `panier/detail.jsp` | `id` | `panier` |
| `/client/reserver` | POST | `ReservationServlet` | — (redirige) | `panierId`, `quantite` | — |
| `/client/annuler` | POST | `ReservationServlet` | — (redirige) | `reservationId` | — |
| `/client/reservations` | GET | `MesReservationsServlet` | `reservation/mes.jsp` | — | `reservations` |
| `/commercant/dashboard` | GET | `CommercantDashboardServlet` | `commercant/dashboard.jsp` | — | `stats` (StatsCommercant), `paniers`, `reservations` |
| `/commercant/paniers` | GET/POST | `PanierCrudServlet` | `commercant/panier-form.jsp` | `action` (`nouveau`, `modifier`, `supprimer`, `enregistrer`), `id`, champs du panier | `panier`, `erreurs` (Map champ → message) |
| `/commercant/retrait` | GET/POST | `RetraitServlet` | `commercant/valider-retrait.jsp` | `codeRetrait` | `retraitsDuJour` |
| `/admin/dashboard` | GET | `AdminDashboardServlet` | `admin/dashboard.jsp` | — | `stats` (StatsAdmin), `utilisateurs` |
| `/admin/utilisateurs` | POST | `AdminUserServlet` | — (redirige) | `action` (`role`, `supprimer`, `creer`), `id`, `role` | — |
| (erreurs) | — | gestion 400/403/404/500 | `error/error.jsp` | — | `codeErreur`, `messageErreur` |

Champs du formulaire panier (noms exacts) : `titre`, `description`, `categorie`, `prixInitial`, `prixReduit`, `quantiteTotale`, `dateDebutRetrait`, `dateFinRetrait` (format `yyyy-MM-dd'T'HH:mm`, champ HTML `datetime-local`).

---

## 10. Données de test (`data.sql`)

Mot de passe de tous les comptes : `password123`

| Email | Rôle |
|---|---|
| `admin@antigaspi.ma` | ADMIN |
| `boulangerie@antigaspi.ma`, `restaurant@antigaspi.ma`, `primeur@antigaspi.ma` | COMMERCANT |
| `sara@antigaspi.ma`, `karim@antigaspi.ma` | CLIENT |

Code de retrait de test pour la démo du parcours : `A1B2C3` (réservation RESERVEE de Sara, commerce Boulangerie).

---

## 11. Checklist de validation (B)

- [ ] Les noms d'attributs correspondent aux maquettes
- [ ] Les attributs de requête listés au §9 suffisent aux JSP
- [ ] Les messages d'erreur du §6 sont corrects
- [ ] Les signatures de services du §5 conviennent
- [ ] Commit : `[E2] contrat validé`
