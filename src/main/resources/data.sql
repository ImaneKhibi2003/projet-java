-- =====================================================================
-- data.sql : données de démonstration (Anti-gaspillage alimentaire)
-- À exécuter APRÈS schema.sql (tables vides : les id démarrent à 1)
-- Mot de passe de TOUS les comptes : password123  (hash BCrypt, coût 10)
-- =====================================================================

-- ---------------------------------------------------------------------
-- UTILISATEURS (id 1 à 6)
-- ---------------------------------------------------------------------
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe_hash, role) VALUES
('Admin',    'Super',  'admin@antigaspi.ma',   '$2a$10$xIi5BlyvAbsoDDWK3ySw0.4QgqFty5IDYA7uSrB5cN8fN9IwEtssq', 'ADMIN'),
('Benali',   'Youssef','boulangerie@antigaspi.ma', '$2a$10$YI0/rnZtzN/8WFEvaGPMQuaFeqhgCt2u6Ht5258xMyVitAiJmHvyC', 'COMMERCANT'),
('Idrissi',  'Fatima', 'restaurant@antigaspi.ma',  '$2a$10$Jd..jgnoT1hAr3iSxDdgAOXYrGuKbvy.KD9CgJiCUkSX5LeLPlwAi', 'COMMERCANT'),
('Tazi',     'Omar',   'primeur@antigaspi.ma',     '$2a$10$v1iJvCsteoQudRg0f4RWNOJ8Vk0H16sXmb6gFyKQ5ZlQe3YY2JxOG', 'COMMERCANT'),
('Alaoui',   'Sara',   'sara@antigaspi.ma',    '$2a$10$hpYtwBs526SsPxXB3dLx3.o5aqce/jCzUgBs5Uzp0Mj7DZsBvL5aq', 'CLIENT'),
('Mansouri', 'Karim',  'karim@antigaspi.ma',   '$2a$10$fq1wUMbkn1mx4XtCAcBS7uCvqLqq3PQYDKMaK9UbVmRV3USheT2uu', 'CLIENT');

-- ---------------------------------------------------------------------
-- COMMERCES (id 1 à 3) : un par commerçant (proprietaire_id = 2, 3, 4)
-- ---------------------------------------------------------------------
INSERT INTO commerce (nom, adresse, ville, telephone, categorie, proprietaire_id) VALUES
('Boulangerie Al Baraka', '12 rue Hassan II',        'Casablanca', '0522000001', 'Boulangerie',       2),
('Restaurant Dar Zitoun', '45 boulevard Zerktouni',  'Casablanca', '0522000002', 'Restaurant',        3),
('Primeur Les Délices',   '8 avenue des FAR',        'Casablanca', '0522000003', 'Fruits et légumes', 4);

-- ---------------------------------------------------------------------
-- PANIERS (id 1 à 6)
-- Fenêtres de retrait larges (jusqu'au 31/12/2026) pour que la démo
-- reste valable pendant les soutenances.
-- ---------------------------------------------------------------------
INSERT INTO panier (commerce_id, titre, description, categorie, prix_initial, prix_reduit,
                    quantite_totale, quantite_restante, date_debut_retrait, date_fin_retrait, statut) VALUES
(1, 'Panier viennoiseries',  'Croissants, pains au chocolat et baguettes du jour', 'Boulangerie',       60.00, 20.00, 5, 4, '2026-11-20 08:00:00', '2026-12-31 20:00:00', 'DISPONIBLE'),
(1, 'Panier pâtisseries',    'Assortiment de gâteaux traditionnels',               'Boulangerie',       80.00, 30.00, 3, 2, '2026-11-20 08:00:00', '2026-12-31 20:00:00', 'DISPONIBLE'),
(2, 'Panier plat du jour',   'Tajine et accompagnements, portion généreuse',       'Restaurant',       100.00, 35.00, 4, 2, '2026-11-20 12:00:00', '2026-12-31 22:00:00', 'DISPONIBLE'),
(2, 'Panier surprise dîner', 'Sélection surprise du chef en fin de service',       'Restaurant',        90.00, 30.00, 3, 3, '2026-11-20 18:00:00', '2026-12-31 22:00:00', 'DISPONIBLE'),
(3, 'Cagette de saison',     'Fruits et légumes frais de saison, 5 kg',            'Fruits et légumes', 70.00, 25.00, 6, 6, '2026-11-20 09:00:00', '2026-12-31 19:00:00', 'DISPONIBLE'),
(3, 'Panier fruits mûrs',    'Fruits très mûrs idéaux pour jus et compotes',       'Fruits et légumes', 40.00, 12.00, 2, 2, '2026-11-10 09:00:00', '2026-11-11 19:00:00', 'EXPIRE');

-- ---------------------------------------------------------------------
-- RESERVATIONS (id 1 à 4)
-- Cohérence : quantite_restante = quantite_totale - réservations actives
-- ---------------------------------------------------------------------
INSERT INTO reservation (panier_id, utilisateur_id, quantite, code_retrait, date_reservation, statut) VALUES
(1, 5, 1, 'A1B2C3', '2026-11-24 09:30:00', 'RESERVEE'),  -- Sara : en attente de retrait
(2, 5, 1, 'D4E5F6', '2026-11-23 10:00:00', 'RETIREE'),   -- Sara : déjà retirée
(3, 6, 2, 'G7H8J9', '2026-11-23 13:15:00', 'RETIREE'),   -- Karim : déjà retirée (2 paniers)
(4, 6, 1, 'K2L3M4', '2026-11-22 17:45:00', 'ANNULEE');   -- Karim : annulée (stock restitué)

-- ---------------------------------------------------------------------
-- RETRAITS (un par réservation RETIREE : 2 et 3)
-- ---------------------------------------------------------------------
INSERT INTO retrait (reservation_id, date_retrait) VALUES
(2, '2026-11-23 18:00:00'),
(3, '2026-11-23 20:30:00');

-- ---------------------------------------------------------------------
-- EVALUATION (optionnelle, sur une réservation retirée)
-- ---------------------------------------------------------------------
INSERT INTO evaluation (reservation_id, note, commentaire, date_evaluation) VALUES
(2, 5, 'Très bons gâteaux, merci pour cette belle initiative !', '2026-11-24 08:00:00');
