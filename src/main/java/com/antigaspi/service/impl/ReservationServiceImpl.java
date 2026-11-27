package com.antigaspi.service.impl;

import com.antigaspi.dao.JpaUtil;
import com.antigaspi.dao.PanierDao;
import com.antigaspi.dao.ReservationDao;
import com.antigaspi.dao.UtilisateurDao;
import com.antigaspi.dao.impl.PanierDaoImpl;
import com.antigaspi.dao.impl.ReservationDaoImpl;
import com.antigaspi.dao.impl.UtilisateurDaoImpl;
import com.antigaspi.exception.BusinessException;
import com.antigaspi.model.Panier;
import com.antigaspi.model.Reservation;
import com.antigaspi.model.Role;
import com.antigaspi.model.StatutPanier;
import com.antigaspi.model.StatutReservation;
import com.antigaspi.model.Utilisateur;
import com.antigaspi.service.CodeGenerator;
import com.antigaspi.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VERSION PROVISOIRE écrite par A pour débloquer les servlets (A4.4, A4.5).
 * La personne B la reprend ou l'améliore (tâches B3.2 et B3.3) en gardant les signatures.
 */
public class ReservationServiceImpl implements ReservationService {

    private final UtilisateurDao utilisateurDao;
    private final PanierDao panierDao;
    private final ReservationDao reservationDao;
    private final CodeGenerator codeGenerator;

    public ReservationServiceImpl() {
        this(new UtilisateurDaoImpl(), new PanierDaoImpl(), new ReservationDaoImpl());
    }

    public ReservationServiceImpl(UtilisateurDao utilisateurDao, PanierDao panierDao,
                                  ReservationDao reservationDao) {
        this.utilisateurDao = utilisateurDao;
        this.panierDao = panierDao;
        this.reservationDao = reservationDao;
        this.codeGenerator = new CodeGenerator(reservationDao);
    }

    @Override
    public Reservation reserver(Long clientId, Long panierId, int quantite) {
        return JpaUtil.inTransaction(() -> {
            Utilisateur client = utilisateurDao.findById(clientId)
                    .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
            if (client.getRole() != Role.CLIENT) {
                throw new BusinessException("Seuls les clients peuvent réserver");
            }
            if (quantite < 1) {
                throw new BusinessException("La quantité doit être au moins 1");
            }

            // Verrou PESSIMISTIC_WRITE : deux clients ne peuvent pas prendre le dernier panier en même temps.
            // Les vérifications ci-dessous se font APRÈS le verrou, sur l'état réel du stock.
            Panier panier = panierDao.findByIdForUpdate(panierId)
                    .orElseThrow(() -> new BusinessException("Panier introuvable"));

            if (panier.getStatut() != StatutPanier.DISPONIBLE) {
                throw new BusinessException("Ce panier n'est plus disponible");
            }
            if (panier.getDateFinRetrait().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Ce panier est expiré");
            }
            if (quantite > panier.getQuantiteRestante()) {
                throw new BusinessException("Quantité insuffisante : il reste "
                        + panier.getQuantiteRestante() + " panier(s)");
            }
            if (reservationDao.existsActiveByUtilisateurAndPanier(clientId, panierId)) {
                throw new BusinessException("Vous avez déjà une réservation en cours pour ce panier");
            }

            // Décrément du stock (écrit à la validation de la transaction)
            panier.setQuantiteRestante(panier.getQuantiteRestante() - quantite);
            if (panier.getQuantiteRestante() == 0) {
                panier.setStatut(StatutPanier.EPUISE);
            }

            Reservation reservation = new Reservation();
            reservation.setPanier(panier);
            reservation.setUtilisateur(client);
            reservation.setQuantite(quantite);
            reservation.setCodeRetrait(codeGenerator.generer());
            reservation.setStatut(StatutReservation.RESERVEE);
            return reservationDao.save(reservation);
        });
    }

    @Override
    public void annuler(Long clientId, Long reservationId) {
        JpaUtil.runInTransaction(() -> {
            Reservation reservation = reservationDao.findById(reservationId)
                    .orElseThrow(() -> new BusinessException("Réservation introuvable"));
            if (!reservation.getUtilisateur().getId().equals(clientId)) {
                throw new BusinessException("Cette réservation ne vous appartient pas");
            }
            if (reservation.getStatut() != StatutReservation.RESERVEE) {
                throw new BusinessException("Seule une réservation en cours peut être annulée");
            }

            // On verrouille le panier avant de lui rendre son stock
            Panier panier = panierDao.findByIdForUpdate(reservation.getPanier().getId())
                    .orElseThrow(() -> new BusinessException("Panier introuvable"));
            panier.setQuantiteRestante(panier.getQuantiteRestante() + reservation.getQuantite());
            if (panier.getStatut() == StatutPanier.EPUISE) {
                panier.setStatut(StatutPanier.DISPONIBLE);
            }
            reservation.setStatut(StatutReservation.ANNULEE);
        });
    }

    @Override
    public List<Reservation> listerParClient(Long clientId) {
        return reservationDao.findByUtilisateur(clientId);
    }

    @Override
    public List<Reservation> listerParCommerce(Long commerceId) {
        return reservationDao.findByCommerce(commerceId);
    }
}
