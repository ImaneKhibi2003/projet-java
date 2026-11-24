-- =====================================================================
-- schema.sql : Anti-gaspillage alimentaire
-- Compatible H2 (mode MySQL) et MySQL 8.0.16+
-- Les "enums" sont des VARCHAR + CHECK (compatible JPA @Enumerated(STRING))
-- =====================================================================

-- Suppression dans l'ordre inverse des dépendances (ré-exécutable)
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS retrait;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS panier;
DROP TABLE IF EXISTS commerce;
DROP TABLE IF EXISTS utilisateur;

-- ---------------------------------------------------------------------
-- UTILISATEUR
-- ---------------------------------------------------------------------
CREATE TABLE utilisateur (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100) NOT NULL,
    email             VARCHAR(150) NOT NULL,
    mot_de_passe_hash VARCHAR(100) NOT NULL,
    role              VARCHAR(20)  NOT NULL,
    date_inscription  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_utilisateur PRIMARY KEY (id),
    CONSTRAINT uq_utilisateur_email UNIQUE (email),
    CONSTRAINT ck_utilisateur_role CHECK (role IN ('CLIENT', 'COMMERCANT', 'ADMIN'))
);

-- ---------------------------------------------------------------------
-- COMMERCE (un commerçant possède au plus un commerce)
-- ---------------------------------------------------------------------
CREATE TABLE commerce (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(150) NOT NULL,
    adresse         VARCHAR(255) NOT NULL,
    ville           VARCHAR(100) NOT NULL,
    telephone       VARCHAR(20),
    categorie       VARCHAR(50)  NOT NULL,
    proprietaire_id BIGINT       NOT NULL,
    CONSTRAINT pk_commerce PRIMARY KEY (id),
    CONSTRAINT uq_commerce_proprietaire UNIQUE (proprietaire_id),
    CONSTRAINT fk_commerce_proprietaire FOREIGN KEY (proprietaire_id)
        REFERENCES utilisateur (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- PANIER
-- ---------------------------------------------------------------------
CREATE TABLE panier (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    commerce_id        BIGINT        NOT NULL,
    titre              VARCHAR(150)  NOT NULL,
    description        VARCHAR(500),
    categorie          VARCHAR(50)   NOT NULL,
    prix_initial       DECIMAL(10,2) NOT NULL,
    prix_reduit        DECIMAL(10,2) NOT NULL,
    quantite_totale    INT           NOT NULL,
    quantite_restante  INT           NOT NULL,
    date_debut_retrait DATETIME      NOT NULL,
    date_fin_retrait   DATETIME      NOT NULL,
    statut             VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE',
    version            BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_panier PRIMARY KEY (id),
    CONSTRAINT fk_panier_commerce FOREIGN KEY (commerce_id)
        REFERENCES commerce (id) ON DELETE CASCADE,
    CONSTRAINT ck_panier_statut CHECK (statut IN ('DISPONIBLE', 'EPUISE', 'EXPIRE')),
    CONSTRAINT ck_panier_prix CHECK (prix_initial > 0 AND prix_reduit >= 0 AND prix_reduit <= prix_initial),
    CONSTRAINT ck_panier_quantites CHECK (quantite_totale > 0
        AND quantite_restante >= 0 AND quantite_restante <= quantite_totale),
    CONSTRAINT ck_panier_dates CHECK (date_fin_retrait > date_debut_retrait)
);

CREATE INDEX idx_panier_commerce  ON panier (commerce_id);
CREATE INDEX idx_panier_statut    ON panier (statut);
CREATE INDEX idx_panier_categorie ON panier (categorie);

-- ---------------------------------------------------------------------
-- RESERVATION
-- ---------------------------------------------------------------------
CREATE TABLE reservation (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    panier_id        BIGINT      NOT NULL,
    utilisateur_id   BIGINT      NOT NULL,
    quantite         INT         NOT NULL DEFAULT 1,
    code_retrait     VARCHAR(6)  NOT NULL,
    date_reservation DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut           VARCHAR(20) NOT NULL DEFAULT 'RESERVEE',
    version          BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_reservation PRIMARY KEY (id),
    CONSTRAINT uq_reservation_code UNIQUE (code_retrait),
    CONSTRAINT fk_reservation_panier FOREIGN KEY (panier_id)
        REFERENCES panier (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id) ON DELETE CASCADE,
    CONSTRAINT ck_reservation_statut CHECK (statut IN ('RESERVEE', 'RETIREE', 'ANNULEE', 'EXPIREE')),
    CONSTRAINT ck_reservation_quantite CHECK (quantite > 0)
);

CREATE INDEX idx_reservation_panier      ON reservation (panier_id);
CREATE INDEX idx_reservation_utilisateur ON reservation (utilisateur_id);
CREATE INDEX idx_reservation_statut      ON reservation (statut);

-- ---------------------------------------------------------------------
-- RETRAIT (au plus un retrait par réservation)
-- ---------------------------------------------------------------------
CREATE TABLE retrait (
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT   NOT NULL,
    date_retrait   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_retrait PRIMARY KEY (id),
    CONSTRAINT uq_retrait_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_retrait_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- EVALUATION (au plus une évaluation par réservation)
-- ---------------------------------------------------------------------
CREATE TABLE evaluation (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT       NOT NULL,
    note            INT          NOT NULL,
    commentaire     VARCHAR(500),
    date_evaluation DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_evaluation PRIMARY KEY (id),
    CONSTRAINT uq_evaluation_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_evaluation_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation (id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluation_note CHECK (note BETWEEN 1 AND 5)
);
