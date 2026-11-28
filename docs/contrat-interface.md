# Contrat d'interface — Anti-gaspillage alimentaire

> Document partagé entre **A** (données, DAO, servlets) et **B** (services, sécurité, JSP).
> Toute modification passe par une Pull Request relue par l'autre personne.
> **Version 1.1** — mise à jour après l'implémentation des étapes 3 et 4 (voir §12).
> Validé par A : ☑ oui  /  validé par B : ☐ oui

---

## 1. Conventions générales

| Élément | Convention |
|---|---|
| Langage / Jakarta | Java 17, Jakarta EE 10, Tomcat 10.1 |
| Package racine | `com.antigaspi` |
| Packages | `model`, `dao`, `dao.impl`, `service`, `service.impl`, `service.dto`, `security`, `exception`, `filter`, `web.auth`, `web.panier`, `web.reservation`, `web.commercant`, `web.admin`, `web.error` |
| Dates | `java.time.LocalDateTime` |
| Montants | `java.math.BigDecimal` (jamais `double`) |
| Identifiants | `Long` |
| Enums en base | `@Enumerated(EnumType.STRING)` |
| Nommage | Classes en français sans accent, attributs en camelCase, colonnes SQL en snake_case |
| Gestion d'erreurs | Les services lèvent `BusinessException` (erreur métier, message affichable) ou `TechnicalException` (erreur technique) |
| Base de données | H2 en mémoire par défaut (unité `antigaspiPU`, données rechargées à chaque démarrage). MySQL/MariaDB (XAMPP) avec l'option VM `-Dantigaspi.pu=antigaspiMysqlPU` et la base `antigaspi` créée à partir de `schema.sql` + `data.sql` |
| JSP | Les JSP lisent les objets via EL (`${...}`) ; les DTO sont des **classes à getters**, pas des records (EL 5.0 de Tomcat 10.1 ne lit pas les records) |

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
| Panier | DISPONIBLE | EPUISE | `quantiteRestante` atteint 0 (réservation) |
| Panier | EPUISE | DISPONIBLE | annulation d'une réservation, ou augmentation du stock par le commerçant |
| Panier | DISPONIBLE | EXPIRE | `dateFinRetrait` dépassée (**à faire : `ExpirationJob`, B3.5**) |
| Panier | EXPIRE | DISPONIBLE | le commerçant modifie le panier avec une nouvelle date de fin future |
| Reservation | RESERVEE | RETIREE | `RetraitService.validerRetrait()` |
| Reservation | RESERVEE | ANNULEE | `ReservationService.annuler()` |
| Reservation | RESERVEE | EXPIREE | `ExpirationJob` (**à faire, B3.5**) |

Les autres transitions sont interdites (ex. RETIREE → ANNULEE).

---

## 3. Entités (package `model`)

Toutes les entités ont getters/setters, un constructeur sans argument, et `equals/hashCode` sur `id`.
Les valeurs automatiques (`dateInscription`, `dateReservation`, `dateRetrait`, `dateEvaluation`, statuts par défaut) sont posées par des méthodes `@PrePersist`.

### Utilisateur — table `utilisateur`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK, auto |
| `nom` | String | `@NotBlank`, max 100 |
| `prenom` | String | `@NotBlank`, max 100 |
| `email` | String | `@NotBlank` `@Email`, unique, enregistré en minuscules |
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
| `proprietaire` | Utilisateur | `@OneToOne`, obligatoire, unique, rôle COMMERCANT |

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
| `dateFinRetrait` | LocalDateTime | `@NotNull`, > `dateDebutRetrait` |
| `statut` | StatutPanier | défaut DISPONIBLE |
| `version` | Long | `@Version` |

> **Changement v1.1** : `@Future` n'est **pas** sur l'entité (Hibernate revalide à chaque mise à jour : impossible de passer un panier à EXPIRE ou de décrémenter son stock une fois la date passée). La règle « la date de fin doit être dans le futur » est vérifiée dans `PanierService.creer()` et `modifier()`.
> L'entité porte trois validations croisées (`@AssertTrue`) : prix réduit ≤ prix initial, fin après début, quantité restante ≤ quantité totale.

### Reservation — table `reservation`
| Attribut | Type | Contraintes |
|---|---|---|
| `id` | Long | PK |
| `panier` | Panier | `@ManyToOne`, obligatoire |
| `utilisateur` | Utilisateur | `@ManyToOne`, le client, obligatoire |
| `quantite` | int | `@Min(1)` |
| `codeRetrait` | String | 6 caractères, unique |
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

## 4. Accès aux données (packages `dao` et `dao.impl`) — responsable : A

### Transactions : `JpaUtil`

```java
// Tout ce qui doit réussir ou échouer ensemble se fait dans UNE transaction :
return JpaUtil.inTransaction(() -> {
    Panier p = panierDao.findByIdForUpdate(id).orElseThrow(...);
    ...
    return reservationDao.save(r);
});
JpaUtil.runInTransaction(() -> { ... });   // sans valeur de retour
```

- Si une transaction est déjà en cours sur le thread, le code la **rejoint**.
- Une exception `RuntimeException` annule la transaction (rollback) puis est relancée.
- Sans transaction, un DAO ouvre une courte transaction par écriture, et lit avec un `EntityManager` temporaire.
- `JpaUtil.reinitialiserBase()` (H2 en mémoire uniquement) recrée les tables et recharge `data.sql` : réservé aux tests.
- Les DAO ne gèrent **pas** les transactions métier : elles sont ouvertes par la couche service.
- Les DAO chargent avec `join fetch` les objets liés utiles aux JSP (commerce d'un panier, panier et client d'une réservation...) : pas de `LazyInitializationException` dans les pages.

### Interfaces

```java
public interface CrudDao<T, ID> {
    T save(T entity);                 // insert si id == null, sinon update
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}

public interface UtilisateurDao extends CrudDao<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);      // insensible à la casse
    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(Role role);
}

public interface CommerceDao extends CrudDao<Commerce, Long> {
    Optional<Commerce> findByProprietaire(Long utilisateurId);
}

public interface PanierDao extends CrudDao<Panier, Long> {
    // DISPONIBLE, quantiteRestante > 0, dateFinRetrait dans le futur.
    // categorie et prixMax peuvent être null (filtre ignoré).
    List<Panier> findDisponibles(String categorie, BigDecimal prixMax);
    List<Panier> findByCommerce(Long commerceId);         // fin de retrait la plus lointaine d'abord
    List<String> findCategories();
    // Verrou PESSIMISTIC_WRITE + relecture : état toujours à jour. Dure jusqu'à la fin de la transaction.
    Optional<Panier> findByIdForUpdate(Long id);
}

public interface ReservationDao extends CrudDao<Reservation, Long> {
    List<Reservation> findByUtilisateur(Long utilisateurId);   // plus récentes d'abord
    List<Reservation> findByCommerce(Long commerceId);         // plus récentes d'abord
    Optional<Reservation> findByCodeRetrait(String code);
    boolean existsByCodeRetrait(String code);
    boolean existsActiveByUtilisateurAndPanier(Long utilisateurId, Long panierId);  // anti-doublon (statut RESERVEE)
    List<Reservation> findExpirees();                          // RESERVEE et panier.dateFinRetrait dépassée
}

public interface RetraitDao extends CrudDao<Retrait, Long> {
    Optional<Retrait> findByReservation(Long reservationId);
    List<Retrait> findByCommerceAndDate(Long commerceId, LocalDate jour);
}
```

Chaque interface a son implémentation dans `dao.impl` (`UtilisateurDaoImpl`, `CommerceDaoImpl`...), qui hérite de `AbstractJpaDao`.

---

## 5. Services (packages `service`, `service.impl`, `service.dto`)

Chaque service a une implémentation dans `service.impl`, avec un constructeur sans argument (qui crée ses DAO) et un constructeur recevant ses DAO (pour les tests avec Mockito).

```java
public interface AuthService {
    Utilisateur login(String email, String motDePasse);                          // BusinessException si échec
    Utilisateur register(String nom, String prenom, String email, String motDePasse); // rôle CLIENT
}

public interface UtilisateurService {
    List<Utilisateur> listerTous();
    Utilisateur trouverParId(Long id);
    Utilisateur creer(Utilisateur u, String motDePasse);                  // CLIENT ou ADMIN seulement
    Utilisateur creerCommercant(Utilisateur u, String motDePasse, Commerce c); // AJOUT v1.1 : compte + commerce, même transaction
    Utilisateur modifier(Utilisateur u);                                  // nom, prénom, email
    void changerRole(Long utilisateurId, Role nouveauRole);
    void supprimer(Long utilisateurId);
}

public interface CommerceService {
    List<Commerce> listerTous();
    Commerce trouverParId(Long id);                       // BusinessException("Commerce introuvable")
    Commerce trouverParProprietaire(Long utilisateurId);  // BusinessException si aucun commerce
    Commerce creer(Commerce c);       // à implémenter
    Commerce modifier(Commerce c);    // à implémenter
    void supprimer(Long id);          // à implémenter
}

public interface PanierService {
    List<Panier> rechercher(String categorie, BigDecimal prixMax, String tri); // tri : "prix" | "date" (défaut)
    Panier trouverParId(Long id);                         // BusinessException("Panier introuvable")
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

`CodeGenerator` (package `service`) : `String generer()`, 6 caractères parmi `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, unicité vérifiée via `ReservationDao.existsByCodeRetrait`.

### DTO de statistiques (package `service.dto`) — classes à getters

```java
StatsCommercant : getNbPaniers(), getNbReservations(), getChiffreAffaires() (BigDecimal), getInvendusSauves()
StatsAdmin      : getNbUtilisateurs(), getNbCommerces(), getNbPaniers(), getNbReservations(),
                  getNbRetraits(), getChiffreAffairesTotal() (BigDecimal), getInvendusSauves()
```

**Définitions** : `invendusSauves` = somme des `quantite` des réservations RETIREE ; `chiffreAffaires` = somme de `prixReduit × quantite` des réservations RETIREE ; `nbReservations` = toutes les réservations reçues, tous statuts.

---

## 6. Règles métier et messages d'erreur

Les messages `BusinessException` ci-dessous sont **exacts** (affichés tels quels dans les JSP).

### Authentification

| Opération | Condition d'échec | Message |
|---|---|---|
| `login` | Mauvais identifiants (email inconnu ou mot de passe faux, même message) | `Email ou mot de passe incorrect` |
| `register` | Email déjà utilisé | `Cet email est déjà utilisé` |
| `register` | Mot de passe < 8 caractères | `Le mot de passe doit contenir au moins 8 caractères` |
| `register` | Champ invalide | message de l'annotation de l'entité (ex. `Email invalide`) |
| servlet `/register` | Confirmation différente | `Les deux mots de passe ne sont pas identiques` |

### Réservation, annulation, retrait

| Opération | Condition d'échec | Message |
|---|---|---|
| `reserver` | Client inconnu | `Utilisateur introuvable` |
| `reserver` | Utilisateur pas CLIENT | `Seuls les clients peuvent réserver` |
| `reserver` | `quantite` < 1 | `La quantité doit être au moins 1` |
| `reserver` | Panier inconnu | `Panier introuvable` |
| `reserver` | Panier non DISPONIBLE | `Ce panier n'est plus disponible` |
| `reserver` | `dateFinRetrait` dépassée | `Ce panier est expiré` |
| `reserver` | `quantite` > `quantiteRestante` | `Quantité insuffisante : il reste X panier(s)` |
| `reserver` | Doublon actif | `Vous avez déjà une réservation en cours pour ce panier` |
| `annuler` | Réservation inconnue | `Réservation introuvable` |
| `annuler` | Réservation d'un autre client | `Cette réservation ne vous appartient pas` |
| `annuler` | Statut ≠ RESERVEE | `Seule une réservation en cours peut être annulée` |
| `validerRetrait` | Code inconnu ou vide | `Code de retrait invalide` |
| `validerRetrait` | Utilisateur sans commerce | `Aucun commerce n'est associé à votre compte` |
| `validerRetrait` | Autre commerce (vérifié **avant** le statut) | `Ce code ne correspond à aucun panier de votre commerce` |
| `validerRetrait` | Statut ≠ RESERVEE | `Cette réservation n'est plus valide (statut : X)` |

`reserver` et `annuler` verrouillent le panier (`findByIdForUpdate`) **avant** de contrôler le stock. `validerRetrait` accepte le code en minuscules et avec des espaces.

### Paniers (commerçant)

| Opération | Condition d'échec | Message |
|---|---|---|
| `creer` / `modifier` | Titre ou catégorie vide | `Le titre est obligatoire` / `La catégorie est obligatoire` |
| `creer` / `modifier` | Quantité < 1 | `La quantité doit être au moins 1` |
| `creer` / `modifier` | Prix négatifs ou nuls | `Les prix doivent être positifs` |
| `creer` / `modifier` | Prix réduit > prix initial | `Le prix réduit doit être inférieur ou égal au prix initial` |
| `creer` / `modifier` | Dates absentes | `Les dates de retrait sont obligatoires` |
| `creer` / `modifier` | Fin avant début | `La date de fin doit être après la date de début` |
| `creer` / `modifier` | Fin dans le passé | `La date de fin doit être dans le futur` |
| `creer` | Utilisateur sans commerce | `Aucun commerce n'est associé à votre compte` |
| `modifier` / `supprimer` | Panier inconnu | `Panier introuvable` |
| `modifier` / `supprimer` | Commerçant non propriétaire | `Vous n'êtes pas propriétaire de ce panier` |
| `modifier` | Total < paniers déjà réservés | `La quantité totale ne peut pas être inférieure aux paniers déjà réservés (N)` |
| `supprimer` | Le panier a des réservations | `Ce panier a déjà des réservations : il ne peut plus être supprimé` |
| servlet `/paniers` | Prix max invalide | `Le prix maximum doit être un nombre positif` |

Règles de `creer` : le commerce est celui du commerçant, `quantiteRestante` = `quantiteTotale`, statut DISPONIBLE (tout id, stock ou statut envoyé par le formulaire est ignoré).
Règles de `modifier` : `quantiteRestante` = nouveau total − paniers déjà pris ; statut recalculé (EPUISE si 0, sinon DISPONIBLE : un panier expiré est remis en vente).

### Utilisateurs (admin)

| Opération | Condition d'échec | Message |
|---|---|---|
| plusieurs | Utilisateur inconnu | `Utilisateur introuvable` |
| `creer` | Rôle COMMERCANT | `Un compte commerçant doit être créé avec son commerce` |
| `creerCommercant` | Commerce absent | `Le commerce est obligatoire pour un commerçant` |
| `creer` / `creerCommercant` / `modifier` | Email déjà utilisé | `Cet email est déjà utilisé` |
| `creer` / `creerCommercant` | Mot de passe < 8 caractères | `Le mot de passe doit contenir au moins 8 caractères` |
| `creerCommercant` | Champ du commerce vide | message de l'entité (ex. `Le nom du commerce est obligatoire`) |
| `changerRole` | Rôle nul ou inconnu | `Rôle invalide` |
| `changerRole` | Retrait du rôle du dernier ADMIN | `Impossible de retirer le rôle du dernier administrateur` |
| `changerRole` | Commerçant avec commerce vers un autre rôle | `Cet utilisateur possède un commerce : son rôle ne peut pas être modifié` |
| `changerRole` | Vers COMMERCANT sans commerce | `Un commerçant doit avoir un commerce : créez un compte commerçant avec son commerce` |
| `supprimer` | Dernier ADMIN | `Impossible de supprimer le dernier administrateur` |
| `supprimer` | Possède un commerce | `Cet utilisateur possède un commerce : il ne peut pas être supprimé` |
| `supprimer` | A des réservations | `Cet utilisateur a des réservations : il ne peut pas être supprimé` |
| servlet `/admin/utilisateurs` | Action sur son propre compte | `Vous ne pouvez pas supprimer votre propre compte` / `Vous ne pouvez pas modifier votre propre rôle` |

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

- `BusinessException` : le servlet affiche le message dans un flash d'erreur (ou dans le formulaire).
- `TechnicalException` et toute autre `RuntimeException` : le servlet journalise (`java.util.logging`) puis appelle `sendError(500)` ; la page d'erreur centralisée s'affiche (§8).

---

## 8. Session, flash, sécurité et erreurs

| Élément | Contrat |
|---|---|
| Utilisateur connecté | `session.setAttribute("utilisateur", utilisateur)` → JSP : `${sessionScope.utilisateur}` ; côté Java : `SessionUtil.utilisateurConnecte(request)` |
| Rôle dans les JSP | `${sessionScope.utilisateur.role == 'ADMIN'}` |
| Session | `SessionUtil.connecter(...)` change l'identifiant de session (anti-fixation), expiration 30 minutes |
| Flash succès / erreur | attributs `flashSucces` / `flashErreur` (String) |
| Mécanisme flash | `FlashUtil.succes(request, msg)` / `FlashUtil.erreur(request, msg)` stockent en session ; `FlashFilter` les place dans la requête puis les supprime. Pour afficher une erreur **sans redirection**, le servlet fait `request.setAttribute(FlashUtil.ERREUR, msg)`. `layout/messages.jsp` lit `${flashSucces}` et `${flashErreur}` |
| Mot de passe | `PasswordUtil.hash(String)` et `PasswordUtil.verify(String, String)` (BCrypt, coût 10) |
| Sécurité des pages | toujours `<c:out>` pour afficher une donnée saisie par un utilisateur |

**Zones protégées par `AuthFilter`** (règles dans `AccesUtil.roleRequis`)

| URL | Accès |
|---|---|
| `/login`, `/register`, `/logout`, `/paniers`, `/panier`, `/erreur`, `/css/*`, `/` | Public |
| `/client/*` | CLIENT |
| `/commercant/*` | COMMERCANT |
| `/admin/*` | ADMIN |

Non connecté sur zone protégée : redirection vers `/login` (l'URL demandée est mémorisée pour après la connexion). Mauvais rôle : erreur **403**.

**Page d'arrivée après connexion** (`AccesUtil.accueil`) : CLIENT → `/paniers`, COMMERCANT → `/commercant/dashboard`, ADMIN → `/admin/dashboard`.

**Erreurs** : `web.xml` redirige les codes 400, 403, 404, 405, 500 et les exceptions non rattrapées vers `ErrorServlet` (`/erreur`), qui journalise puis affiche `error/error.jsp`.

---

## 9. Routes web : servlets (A) ↔ JSP (B)

JSP placées sous `WEB-INF/views/`. Les attributs de requête listés sont ceux que le servlet **fournit** à la JSP.

| URL | Méthode | Servlet (A) | JSP (B) | Paramètres | Attributs fournis |
|---|---|---|---|---|---|
| `/login` | GET/POST | `LoginServlet` | `auth/login.jsp` | `email`, `motDePasse` | `email` (réaffichage) |
| `/register` | GET/POST | `RegisterServlet` | `auth/register.jsp` | `nom`, `prenom`, `email`, `motDePasse`, `confirmation` | `nom`, `prenom`, `email` (réaffichage) |
| `/logout` | GET/POST | `LogoutServlet` | — | — | — |
| `/paniers` | GET | `PanierListServlet` | `panier/list.jsp` | `categorie`, `prixMax`, `tri`, `page` | `paniers` (page courante, 6 par page), `categories`, `page`, `nbPages`, `total`, `categorie`, `prixMaxSaisi`, `tri`, `filtres` (paramètres à conserver dans les liens de pagination, à passer dans `<c:out>`) |
| `/panier` | GET | `PanierDetailServlet` | `panier/detail.jsp` | `id` | `panier`, `reservable` (boolean), `messageReservation` (raison si non réservable), `maxQuantite` |
| `/client/reserver` | POST | `ReservationServlet` | — (redirige vers `/client/reservations`) | `panierId`, `quantite` | — |
| `/client/annuler` | POST | `ReservationServlet` | — (redirige vers `/client/reservations`) | `reservationId` | — |
| `/client/reservations` | GET | `MesReservationsServlet` | `reservation/mes.jsp` | — | `reservations` |
| `/commercant/dashboard` | GET | `CommercantDashboardServlet` | `commercant/dashboard.jsp` | — | `commerce`, `stats` (StatsCommercant), `paniers`, `reservations` |
| `/commercant/paniers` | GET | `PanierCrudServlet` | `commercant/panier-form.jsp` | `action` (`nouveau` par défaut, ou `modifier`), `id` | `panier` (formulaire, voir ci-dessous), `erreurs`, `modeEdition` |
| `/commercant/paniers` | POST | `PanierCrudServlet` | `commercant/panier-form.jsp` en cas d'erreur, sinon redirection vers `/commercant/dashboard` | `action` (`enregistrer` ou `supprimer`), `id`, champs du panier | idem |
| `/commercant/retrait` | GET | `RetraitServlet` | `commercant/valider-retrait.jsp` | — | `retraitsDuJour` |
| `/commercant/retrait` | POST | `RetraitServlet` | — (redirige vers `/commercant/retrait` avec un flash) | `codeRetrait` | — |
| `/admin/dashboard` | GET | `AdminDashboardServlet` | `admin/dashboard.jsp` | — | `stats` (StatsAdmin), `utilisateurs` |
| `/admin/utilisateurs` | GET | `AdminUserServlet` | `admin/utilisateur-form.jsp` | `action` (`nouveau` ou `modifier`), `id` | `valeurs` (Map champ → texte), `modeEdition`, `idEdition` |
| `/admin/utilisateurs` | POST | `AdminUserServlet` | `admin/utilisateur-form.jsp` en cas d'erreur, sinon redirection vers `/admin/dashboard` | `action` (`creer`, `modifier`, `role`, `supprimer`), `id`, `role`, champs de l'utilisateur | idem |
| `/erreur` | toutes | `ErrorServlet` (via `web.xml`) | `error/error.jsp` | — | `codeErreur`, `titreErreur`, `messageErreur`, `referenceErreur` (500 seulement), `lienRetour`, `libelleRetour` |

**Formulaire panier** (`/commercant/paniers`) : champs `titre`, `description`, `categorie`, `prixInitial`, `prixReduit`, `quantiteTotale`, `dateDebutRetrait`, `dateFinRetrait` (format `yyyy-MM-dd'T'HH:mm`, champ HTML `datetime-local`), plus `action=enregistrer` et `id` (en modification).
L'attribut `panier` est un **formulaire dont toutes les propriétés sont du texte** (mêmes noms que l'entité) ; `erreurs` est une `Map` champ → message (`${erreurs.titre}`, `${erreurs.prixReduit}`...).

**Formulaire utilisateur** (`/admin/utilisateurs`) : champs `nom`, `prenom`, `email`, et à la création `motDePasse`, `role`. Si `role` = `COMMERCANT` : `commerceNom`, `commerceAdresse`, `commerceVille`, `commerceCategorie`, `commerceTelephone` (facultatif).

Un formulaire qui modifie des données se soumet toujours en **POST** ; les identifiants de l'utilisateur ou du commerçant ne viennent **jamais** du formulaire (ils viennent de la session).

---

## 10. Données de test (`data.sql`)

Mot de passe de tous les comptes : `password123`

| Email | Rôle | Remarque |
|---|---|---|
| `admin@antigaspi.ma` | ADMIN | |
| `boulangerie@antigaspi.ma` | COMMERCANT | Boulangerie Al Baraka (paniers 1 et 2) |
| `restaurant@antigaspi.ma` | COMMERCANT | Restaurant Dar Zitoun (paniers 3 et 4) |
| `primeur@antigaspi.ma` | COMMERCANT | Primeur Les Délices (paniers 5 et 6, le 6 est expiré) |
| `sara@antigaspi.ma` | CLIENT | réservation RESERVEE, code `A1B2C3` (boulangerie) |
| `karim@antigaspi.ma` | CLIENT | |

Jeu de données : 6 utilisateurs, 3 commerces, 6 paniers, 4 réservations (RESERVEE, RETIREE, RETIREE, ANNULEE), 2 retraits, 1 évaluation. Les fenêtres de retrait vont jusqu'au 31/12/2026.

---

## 11. État d'avancement et reprise par B

Pour débloquer les servlets, **A a écrit des versions provisoires** de classes de B, conformes à ce contrat. B peut les reprendre et les améliorer **en gardant les signatures** (pas de réécriture de zéro : risque de conflits Git).

| Tâche de B | Déjà présent (provisoire, écrit par A) | Reste à faire |
|---|---|---|
| B3.1 Services | `AuthServiceImpl`, `PanierServiceImpl`, `ReservationServiceImpl`, `RetraitServiceImpl`, `UtilisateurServiceImpl`, `StatsServiceImpl`, `CommerceServiceImpl` (lectures seulement) | `CommerceService.creer/modifier/supprimer` |
| B3.2 / B3.3 / B3.4 | `reserver`, `annuler`, `validerRetrait` + tests | relecture, cas limites |
| B3.5 Expiration | — | `ExpirationJob` (sur Tomcat : `ScheduledExecutorService` lancé par un `ServletContextListener`, pas `@Schedule`) |
| B3.6 / B3.8 / B3.9 / B3.10 | `CodeGenerator`, exceptions, `PasswordUtil`, `AuthFilter` | relecture |
| B3.7 Validation | annotations sur les entités | — |
| B3.11 Tests services | tests de tous les services (écrits par A) | tests Mockito complémentaires |
| B4.1 à B4.10 | JSP provisoires sans style | **mise en forme** |
| B4.11 / B4.12 | — | CSS (`app.css`), contrôle d'accès dans les JSP |

---

## 12. Historique des modifications

| Version | Changement |
|---|---|
| 1.0 | Contrat initial (étape 2) |
| 1.1 | Packages `dao.impl`, `service.impl`, `service.dto` ; `JpaUtil` et gestion des transactions ; `@Future` retiré de l'entité `Panier` (règle déplacée dans le service) ; DTO de statistiques en **classes à getters** (EL 5.0 ne lit pas les records) ; ajout de `UtilisateurService.creerCommercant` ; `CommerceService.trouverParProprietaire` lève `BusinessException` ; code de retrait sans `I`, `O`, `0`, `1` ; messages d'erreur complétés (§6) ; routes et attributs des JSP complétés (§9) ; gestion centralisée des erreurs (§8) ; `AccesUtil.accueil` par rôle |

---

## 13. Checklist de validation (B)

- [ ] Les noms d'attributs correspondent aux maquettes
- [ ] Les attributs de requête listés au §9 suffisent aux JSP
- [ ] Les messages d'erreur du §6 sont corrects
- [ ] Les signatures de services du §5 conviennent
- [ ] B a lu le §11 et sait quelles classes sont provisoires
- [ ] Commit : `[E2] contrat validé`
