package com.antigaspi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "panier")
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commerce_id", nullable = false)
    private Commerce commerce;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titre;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categorie;

    @NotNull(message = "Le prix initial est obligatoire")
    @Positive(message = "Le prix initial doit être positif")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixInitial;

    @NotNull(message = "Le prix réduit est obligatoire")
    @PositiveOrZero(message = "Le prix réduit ne peut pas être négatif")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixReduit;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(nullable = false)
    private int quantiteTotale;

    @Min(0)
    @Column(nullable = false)
    private int quantiteRestante;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateDebutRetrait;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateFinRetrait;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPanier statut;

    @Version
    private Long version;

    public Panier() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Commerce getCommerce() {
        return commerce;
    }

    public void setCommerce(Commerce commerce) {
        this.commerce = commerce;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getPrixInitial() {
        return prixInitial;
    }

    public void setPrixInitial(BigDecimal prixInitial) {
        this.prixInitial = prixInitial;
    }

    public BigDecimal getPrixReduit() {
        return prixReduit;
    }

    public void setPrixReduit(BigDecimal prixReduit) {
        this.prixReduit = prixReduit;
    }

    public int getQuantiteTotale() {
        return quantiteTotale;
    }

    public void setQuantiteTotale(int quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    public int getQuantiteRestante() {
        return quantiteRestante;
    }

    public void setQuantiteRestante(int quantiteRestante) {
        this.quantiteRestante = quantiteRestante;
    }

    public LocalDateTime getDateDebutRetrait() {
        return dateDebutRetrait;
    }

    public void setDateDebutRetrait(LocalDateTime dateDebutRetrait) {
        this.dateDebutRetrait = dateDebutRetrait;
    }

    public LocalDateTime getDateFinRetrait() {
        return dateFinRetrait;
    }

    public void setDateFinRetrait(LocalDateTime dateFinRetrait) {
        this.dateFinRetrait = dateFinRetrait;
    }

    public StatutPanier getStatut() {
        return statut;
    }

    public void setStatut(StatutPanier statut) {
        this.statut = statut;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @PrePersist
    void avantCreation() {
        if (statut == null) {
            statut = StatutPanier.DISPONIBLE;
        }
    }

    // Validations croisées (Bean Validation, évaluées à l'enregistrement)
    @AssertTrue(message = "Le prix réduit doit être inférieur ou égal au prix initial")
    public boolean isPrixCoherent() {
        return prixInitial == null || prixReduit == null || prixReduit.compareTo(prixInitial) <= 0;
    }

    @AssertTrue(message = "La date de fin doit être après la date de début")
    public boolean isDatesCoherentes() {
        return dateDebutRetrait == null || dateFinRetrait == null || dateFinRetrait.isAfter(dateDebutRetrait);
    }

    @AssertTrue(message = "La quantité restante ne peut pas dépasser la quantité totale")
    public boolean isQuantitesCoherentes() {
        return quantiteRestante <= quantiteTotale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Panier other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Panier[id=" + id + "]";
    }
}
