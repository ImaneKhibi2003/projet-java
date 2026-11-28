# Guide de démarrage — Houda (Personne B)

Projet **Anti-gaspillage alimentaire** (sujet N°3, Jakarta EE) — dépôt : <https://github.com/ImaneKhibi2003/projet-java>

Ce guide te permet de **récupérer le projet, le lancer et savoir exactement quoi faire**. Lis-le en entier une fois, puis suis les étapes dans l'ordre.

> **À retenir tout de suite**
> - Ta branche s'appelle **`feature/houda`**. Tu ne travailles jamais directement sur `main`.
> - Tu dois faire **au moins 1 commit par jour** (exigence du prof). Même un petit commit compte.
> - Chaque membre sera interrogé **individuellement** à la soutenance : tu dois comprendre le code de ta partie, y compris les classes que Imane a écrites en version provisoire pour te débloquer (voir §6).

---

## 1. Ce qu'il faut installer

| Outil | Version | Où le télécharger | Remarque |
|---|---|---|---|
| **JDK** | **17** | <https://adoptium.net> (Temurin 17) | Pas Java 8, 11 ou 21 |
| **IntelliJ IDEA** | récente | <https://www.jetbrains.com/idea/download> | *Ultimate* gère Tomcat directement (licence gratuite avec ton e-mail d'étudiante : <https://www.jetbrains.com/community/education>). La version *Community* marche aussi avec le plugin gratuit **Smart Tomcat** |
| **Git** | récente | <https://git-scm.com/downloads> | Sur Mac, `git --version` propose parfois de l'installer |
| **Tomcat** | **10.1.x** (pas la 9, pas la 11) | <https://tomcat.apache.org/download-10.cgi> → *Core* → `zip` (Windows) ou `tar.gz` (Mac/Linux) | Décompresse-le dans un dossier simple, par exemple `Documents/apache-tomcat-10.1.xx` |
| **Compte GitHub** | — | <https://github.com> | Avec l'e-mail utilisé pour l'invitation |
| *XAMPP* (facultatif) | — | <https://www.apachefriends.org> | Seulement si tu veux tester avec MySQL. Par défaut le projet utilise H2 (rien à installer) |

**Maven : rien à installer.** Le projet contient `mvnw` (Mac/Linux) et `mvnw.cmd` (Windows) qui téléchargent Maven tout seuls. À la première compilation, Maven télécharge aussi **toutes les dépendances** (Hibernate, H2, JSTL, BCrypt, JUnit, Mockito…) : il faut donc une connexion internet et un peu de patience.

**Vérifier les installations** (dans un terminal) :

```
java -version        # doit afficher 17.x
git --version
```

---

## 2. Récupérer le projet depuis GitHub

### 2.1 Accepter l'invitation

Imane t'a invitée comme collaboratrice. Ouvre l'e-mail de GitHub et clique sur **Accept invitation**. Sinon, va sur <https://github.com/ImaneKhibi2003/projet-java/invitations>.

Sans cette étape, tu ne pourras pas envoyer ton travail.

### 2.2 Dire à Git qui tu es (une seule fois)

```
git config --global user.name "Houda <ton nom>"
git config --global user.email "ton-email-github@exemple.com"
```

Utilise **la même adresse e-mail que ton compte GitHub**, sinon tes commits ne seront pas reliés à ton profil et ne compteront pas pour le prof.

### 2.3 Cloner le dépôt

**Avec IntelliJ** (le plus simple) :

1. Écran d'accueil → **Get from VCS** (ou *File → New → Project from Version Control*).
2. **URL** : `https://github.com/ImaneKhibi2003/projet-java.git`
3. Choisis un dossier, par exemple `Documents/projet-java`, puis **Clone**.
4. IntelliJ te propose de te connecter à GitHub : accepte (*Log in via GitHub*, une page s'ouvre dans le navigateur).

**Ou en ligne de commande :**

```
cd Documents
git clone https://github.com/ImaneKhibi2003/projet-java.git
cd projet-java
```

Si Git demande un mot de passe : GitHub n'accepte plus le mot de passe du compte, il faut un **Personal Access Token** (GitHub → *Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate*, coche `repo`) à coller à la place du mot de passe.

---

## 3. Créer ta branche `feature/houda`

Toujours à partir d'un `main` à jour :

```
git checkout main
git pull origin main
git checkout -b feature/houda
git push -u origin feature/houda
```

**Dans IntelliJ** : clique sur le nom de la branche en bas à droite (ou en haut à gauche) → **New Branch…** → nom `feature/houda` → Create. Le `git push -u` est à faire une fois dans le terminal pour publier la branche.

Vérifie sur quelle branche tu es :

```
git branch
```

L'étoile `*` doit être devant `feature/houda`.

---

## 4. Ouvrir et lancer le projet

### 4.1 Ouvrir et configurer IntelliJ

1. Ouvre le dossier cloné (*File → Open*). Si IntelliJ demande « Trust project », accepte.
2. Si une bulle propose de **charger le projet Maven**, accepte. Sinon, clique sur l'icône **Load Maven Changes** (petit « m » en haut à droite de l'éditeur, ou dans l'onglet Maven à droite).
3. Vérifie le JDK : *File → Project Structure → Project → SDK* = **17**. Si absent : *Add SDK → Download JDK → Temurin 17*.
4. Attends que la barre de progression en bas se termine (téléchargement des dépendances).

### 4.2 Lancer les tests

Dans le terminal d'IntelliJ, à la racine du projet :

```
./mvnw test          # Mac / Linux
mvnw.cmd test        # Windows
```

Sur Mac/Linux, si tu as « Permission denied » : `chmod +x mvnw` puis recommence.

Résultat attendu : **`BUILD SUCCESS`** et environ 150 tests, tous verts. Dans les logs, une ligne `ERROR: Unique index or primary key violation…` est **normale** (un test provoque exprès un email en double).

### 4.3 Lancer l'application

**IntelliJ Ultimate :**

1. **Run → Edit Configurations… → + → Tomcat Server → Local**.
2. **Application server** : *Configure…* puis choisis ton dossier Tomcat 10.1.
3. Onglet **Deployment** : **+ → Artifact → `antigaspi:war exploded`**. Dans *Application context*, mets `/antigaspi`.
4. Onglet **Server**, **Before launch** : **+ → Run Maven Goal →** écris `package` (ainsi le projet est reconstruit à chaque lancement).
5. Clique sur ▶.

**IntelliJ Community :** installe le plugin **Smart Tomcat** (*Settings → Plugins*), puis crée une configuration *Smart Tomcat* : dossier Tomcat, contexte `/antigaspi`, et lance-la.

Ouvre ensuite : <http://localhost:8080/antigaspi/> puis `/login`.

> Par défaut, la base est **H2 en mémoire** : les données (`data.sql`) sont rechargées à chaque démarrage, et ce que tu crées disparaît à l'arrêt. C'est voulu pour développer.
> Pour MySQL (XAMPP) : crée la base `antigaspi`, importe `schema.sql` puis `data.sql` (dans `src/main/resources`) via phpMyAdmin, et ajoute dans les *VM options* de Tomcat : `-Dantigaspi.pu=antigaspiMysqlPU`.

### 4.4 Comptes de test (mot de passe : `password123`)

| Email | Rôle |
|---|---|
| `admin@antigaspi.ma` | Admin |
| `boulangerie@antigaspi.ma`, `restaurant@antigaspi.ma`, `primeur@antigaspi.ma` | Commerçants |
| `sara@antigaspi.ma`, `karim@antigaspi.ma` | Clients |

**Parcours à essayer pour comprendre l'application :** connecte-toi en `karim@antigaspi.ma`, réserve un panier (tu reçois un code de retrait), déconnecte-toi, connecte-toi avec le commerçant du panier, saisis le code dans « Valider un retrait », puis regarde le tableau de bord.

---

## 5. Où se trouve quoi

```
projet-java/
├── docs/                         contrat d'interface, diagrammes, ce guide
├── src/main/java/com/antigaspi/
│   ├── model/                    entités JPA + énumérations
│   ├── dao/ et dao/impl/         accès à la base (écrit par Imane) + JpaUtil (transactions)
│   ├── service/ et service/impl/ logique métier  ← TA PARTIE
│   ├── security/                 PasswordUtil, AccesUtil
│   ├── exception/                BusinessException, TechnicalException
│   ├── filter/                   AuthFilter, FlashFilter
│   └── web/                      servlets (écrits par Imane)
├── src/main/resources/
│   ├── META-INF/persistence.xml  configuration JPA (H2 par défaut, MySQL en option)
│   ├── schema.sql, data.sql      tables et données de démo
├── src/main/webapp/
│   ├── css/                      ← TA PARTIE (app.css à créer)
│   └── WEB-INF/views/            pages JSP  ← TA PARTIE
└── src/test/java/                tests JUnit
```

**Le document le plus important : `docs/contrat-interface.md`.** Il fixe les noms des classes, les signatures des services, les messages d'erreur et, pour chaque URL, les attributs que les servlets fournissent aux JSP (§9). Si tu veux changer quelque chose qui y figure, **parles-en d'abord à Imane**.

---

## 6. Tes tâches

### 6.1 Ce qui existe déjà

Comme tu n'avais pas encore commencé, Imane a écrit des **versions provisoires** de classes qui t'étaient destinées, en respectant les signatures du contrat, pour pouvoir tester le parcours complet. Tu les **reprends et tu les améliores**, tu ne les réécris pas de zéro (sinon vos deux branches se contrediront dans Git).

| Ta tâche | État |
|---|---|
| B3.1 Couche service | Services écrits en version provisoire (`service/impl`) — à relire et compléter |
| B3.2 Règle réservation | `ReservationServiceImpl.reserver()` écrit et testé — à relire, **comprendre le verrou** |
| B3.3 Règle annulation | `annuler()` écrit et testé |
| B3.4 Règle retrait | `RetraitServiceImpl.validerRetrait()` écrit et testé |
| **B3.5 Expiration** | **À faire entièrement** (voir 6.4) |
| B3.6 Générateur de code | `CodeGenerator` écrit et testé |
| B3.7 Bean Validation | Annotations déjà dans les entités |
| B3.8 Exceptions | `BusinessException`, `TechnicalException`, gestion centralisée des erreurs écrites |
| B3.9 Authentification | `PasswordUtil`, `AuthServiceImpl` écrits et testés |
| B3.10 Filtre de sécurité | `AuthFilter` écrit |
| B3.11 Tests services | Tests écrits pour tous les services — à compléter |
| B4.1 à B4.10 Pages JSP | **Pages provisoires sans mise en forme** — à styliser |
| **B4.11 CSS** | **À faire entièrement** |
| **B4.12 Contrôle d'accès UI** | À faire (masquer menus et boutons selon le rôle) |
| **B2.1 à B2.5 Conception** | **À faire** (cas d'utilisation, maquettes…) — voir 6.5 |

### 6.2 Ordre de travail conseillé

| # | Quoi | Pourquoi maintenant |
|---|---|---|
| 1 | **Installer, cloner, lancer l'appli, créer `feature/houda`** (§1 à §4) | Sans ça rien d'autre n'est possible |
| 2 | **Premier commit** (même minime) | Le prof veut des commits de chaque membre dès le début |
| 3 | **CSS et mise en forme des pages** (B4.11, B4.1 à B4.10) | Rend la démo du 29 novembre présentable |
| 4 | **Contrôle d'accès dans les JSP** (B4.12) | Rapide, améliore l'interface |
| 5 | **Documents de conception** (B2.1 à B2.5) | Livrables obligatoires du dossier (attendus à l'origine pour le 15 novembre) |
| 6 | **`ExpirationJob`** (B3.5) + tests | Règle métier manquante |
| 7 | Relecture et tests complémentaires des services (B3.1 à B3.4, B3.11) | Pour pouvoir **expliquer** ton code à la soutenance |

### 6.3 Mise en forme des pages (B4.1 à B4.12)

Les pages sont dans `src/main/webapp/WEB-INF/views/`. Elles fonctionnent, mais utilisent des styles « en ligne » provisoires.

À faire :

1. Créer **`src/main/webapp/css/app.css`** : charte graphique écolo (vert `#2e7d32` et orange `#ff9800` déjà utilisés), responsive, boutons, cartes de paniers, tableaux, **badges de statut** (Réservée, Retirée, Annulée, Expirée, Disponible, Épuisé), alertes de succès et d'erreur.
2. Créer les fichiers de layout (`layout/header.jsp`, `layout/footer.jsp`) et reprendre `layout/nav.jsp` et `layout/messages.jsp`, puis les **inclure dans toutes les pages** avec `<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>`. Charger la feuille de style dans chaque page :

   ```jsp
   <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
   ```

3. Remplacer les `style="..."` des pages (`panier/list.jsp`, `panier/detail.jsp`, `reservation/mes.jsp`, `commercant/dashboard.jsp`, `commercant/panier-form.jsp`, `commercant/valider-retrait.jsp`, `admin/dashboard.jsp`, `admin/utilisateur-form.jsp`, `auth/login.jsp`, `auth/register.jsp`, `error/error.jsp`) par des classes CSS.
4. **Contrôle d'accès dans l'interface** (B4.12) : masquer ou afficher menus et boutons selon le rôle, par exemple

   ```jsp
   <c:if test="${sessionScope.utilisateur.role == 'ADMIN'}"> ... </c:if>
   ```

   (le menu `nav.jsp` le fait déjà en partie : à compléter et à vérifier).

**Règles à respecter quand tu modifies une page :**

- Ne change **pas** les noms des champs de formulaire (`name="..."`) ni les URL d'action : les servlets les attendent (voir §9 du contrat).
- Garde les `<c:out value="...">` autour de tout texte venant d'un utilisateur (protection contre l'injection de code dans les pages).
- Les formulaires qui modifient des données restent en **POST**.
- Garde les attributs d'erreur : `${erreurs.titre}`, `${flashErreur}`, etc.

### 6.4 `ExpirationJob` (B3.5) : à écrire

**Objectif :** toutes les 15 minutes, passer les réservations `RESERVEE` dont la fenêtre de retrait est dépassée en `EXPIREE` et **rendre leur stock** au panier. Passer aussi en `EXPIRE` les paniers dont la date de fin est dépassée.

**Attention :** ton plan parlait de `@Schedule` (EJB timer). **Tomcat ne gère pas les EJB.** Il faut utiliser un `ScheduledExecutorService` démarré par un `ServletContextListener`.

Squelette (à adapter, dans `com.antigaspi.service`) :

```java
@WebListener
public class ExpirationJob implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ExpirationJob.class.getName());
    private ScheduledExecutorService planificateur;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ReservationService service = new ReservationServiceImpl();
        planificateur = Executors.newSingleThreadScheduledExecutor();
        planificateur.scheduleAtFixedRate(() -> {
            try {
                int nb = service.expirerReservations();
                if (nb > 0) {
                    LOG.info(nb + " réservation(s) expirée(s)");
                }
            } catch (RuntimeException e) {
                // indispensable : une exception non rattrapée arrêterait toute la planification
                LOG.log(Level.SEVERE, "Échec du traitement d'expiration", e);
            }
        }, 1, 15, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (planificateur != null) {
            planificateur.shutdownNow();
        }
    }
}
```

À ajouter dans `ReservationService` (et dans le contrat, §5, **après accord avec Imane**) : `int expirerReservations();` qui retourne le nombre de réservations traitées. Dans une **seule transaction** :

1. `reservationDao.findExpirees()` (existe déjà et est testée).
2. Pour chacune : verrouiller le panier (`panierDao.findByIdForUpdate`), lui rendre la quantité, passer la réservation à `EXPIREE`.
3. Passer en `EXPIRE` les paniers dont `dateFinRetrait` est dépassée et qui sont encore `DISPONIBLE` ou `EPUISE`.

C'est la même logique que `annuler()` (déjà écrit dans `ReservationServiceImpl`) : lis-le d'abord.

**Tests à écrire** (JUnit, comme `ReservationServiceTest`) : une réservation sur le panier 6 (déjà expiré dans `data.sql`) est passée à `EXPIREE` ; le stock est rendu ; une réservation encore dans sa fenêtre n'est pas touchée ; le service ne traite pas deux fois la même réservation.

### 6.5 Documents de conception (B2.1 à B2.5)

Ils sont à ajouter dans le dossier `docs/`. Le plan et les consignes sont dans `Mini-projets_Java_avancé_sujets_et_feuille_de_route.pdf` (livrables 2 et 3).

| # | Livrable | Fichier |
|---|---|---|
| B2.1 | Diagramme de cas d'utilisation. Acteurs : Client, Commerçant, Admin. Cas : s'authentifier, consulter les paniers, réserver, annuler, retirer, publier un panier, valider un retrait, gérer les utilisateurs, voir le tableau de bord | `docs/cas-utilisation.png` (PlantUML ou draw.io) |
| B2.2 | Description textuelle de chaque cas : préconditions, scénario nominal, scénarios alternatifs, postconditions | `docs/cas-utilisation-descriptions.md` |
| B2.3 | Maquettes de 8 écrans minimum : connexion/inscription, liste des paniers, détail, mes réservations, tableau de bord commerçant, formulaire de panier, validation du retrait, tableau de bord admin | lien Figma/Penpot ou captures dans `docs/maquettes/` |
| B2.4 | Parcours utilisateur : client → réservation → code → commerçant valide → statut RETIREE | `docs/parcours-utilisateur.png` |
| B2.5 | Validation du contrat d'interface : relis `docs/contrat-interface.md`, vérifie les points de la checklist (§13), puis fais le commit « contrat validé » | commit `[E2] contrat validé` |

**Astuce :** l'application tourne déjà. Tu peux faire des **captures d'écran des vraies pages** pour t'aider à dessiner les maquettes et le parcours, et lire les messages et les règles dans le contrat (§6) pour les descriptions de cas d'utilisation.

---

## 7. Travailler avec Git au quotidien

### 7.1 Routine de chaque séance

```
git checkout feature/houda
git fetch origin
git merge origin/main        # récupère ce que Imane a fusionné dans main
```

Puis tu travailles. À chaque étape terminée :

```
git status                   # vérifie les fichiers modifiés (jamais target/ ni .idea/)
git add .
git commit -m "[E4] CSS : charte graphique et cartes de paniers"
git push origin feature/houda
```

### 7.2 Messages de commit

Format : `[E<étape>] description courte`.

```
[E4] CSS : badges de statut
[E4] JSP liste paniers : mise en forme
[E3] ExpirationJob : expiration des réservations
[E2] cas d'utilisation + descriptions
```

**1 commit minimum par jour** — mais plusieurs petits commits valent mieux qu'un gros.

### 7.3 Envoyer ton travail dans `main` (Pull Request)

Un `push` envoie ton travail sur **ta branche** seulement. Pour qu'il arrive dans `main` :

1. Sur <https://github.com/ImaneKhibi2003/projet-java>, clique sur **Compare & pull request** (bandeau jaune) ou *Pull requests → New pull request*.
2. Vérifie `base: main` ← `compare: feature/houda`, mets un titre clair, puis **Create pull request**.
3. Imane relit et fusionne (**Merge pull request**, type par défaut « merge commit »). Ne coche pas « Squash », et ne supprime pas ta branche.

### 7.4 En cas de problème

| Message | Que faire |
|---|---|
| `rejected … non-fast-forward` au push | Quelqu'un a poussé avant toi : `git pull origin feature/houda`, puis refais le push |
| Conflit au `git merge origin/main` | Ne force rien. Envoie à Imane le message ou la liste des fichiers en conflit. IntelliJ propose aussi un outil de fusion (*Resolve*) |
| `target/` ou `.idea/` apparaissent dans `git status` | Ne les ajoute pas. Si déjà ajoutés : `git rm -r --cached target .idea` |
| Tu as travaillé sur `main` par erreur | `git checkout -b feature/houda` (garde tes modifications), puis commit sur cette branche |
| Tu as peur de casser quelque chose | Fais un commit avant chaque grosse modification : on peut toujours revenir en arrière |

---

## 8. Dépannage

| Symptôme | Cause probable et solution |
|---|---|
| `target/antigaspi not found for the web module` au lancement | Le projet n'est pas construit. Onglet **Maven → Lifecycle → clean puis package**, ou ajoute *Run Maven Goal → package* dans *Before launch* |
| **Permission denied** sur `mvnw` ou `catalina.sh` (Mac/Linux) | `chmod +x mvnw` et `chmod +x <dossier-tomcat>/bin/*.sh` |
| Erreur 404 sur `/antigaspi/login` | Le contexte n'est pas `/antigaspi` (onglet *Deployment*), ou le déploiement a échoué : regarde la console |
| `Port 8080 was already in use` | Un autre Tomcat tourne : arrête-le (carré rouge) ou ferme l'ancienne fenêtre |
| `The absolute uri: jakarta.tags.core cannot be resolved` | Dépendances non chargées : *Load Maven Changes*, puis clean + package |
| `release version 17 not supported` ou erreurs de langage | Le JDK du projet n'est pas 17 (*Project Structure → SDK*), et aussi *Settings → Build Tools → Maven → Runner → JRE* |
| Une page ne reflète pas ta modification | Redémarre Tomcat ou utilise *Update application* (Cmd/Ctrl + F10), puis recharge la page en forçant (Cmd/Ctrl + Maj + R) |
| Les accents s'affichent mal | Vérifie que `<%@ page contentType="text/html;charset=UTF-8" %>` est en haut de la page, et que tes fichiers sont en UTF-8 (barre d'état d'IntelliJ) |
| Tests rouges chez toi alors qu'ils passent chez Imane | Envoie-lui le nom du test et le message d'erreur (les lignes `Caused by:`) |

---

## 9. Aide-mémoire

```
# Lancer les tests
./mvnw test                  (Windows : mvnw.cmd test)

# Construire le .war
./mvnw clean package

# Mettre à jour ta branche avec main
git fetch origin && git merge origin/main

# Voir l'état et l'historique
git status
git log --oneline -10
```

**Calendrier :** revue de code et démonstration de la version 1 le **29 novembre** ; version 2 (tableau de bord, recherche, export CSV/PDF, fonctionnalité spécifique) le **6 décembre** ; dépôt du code final le **14 décembre** ; soutenance au plus tard le **19 décembre**.

Une question ou un blocage ? Écris à Imane avec : **ce que tu as fait, ce que tu attendais, ce que tu obtiens, et le message d'erreur complet**.
