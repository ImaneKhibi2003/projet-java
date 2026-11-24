package com.antigaspi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "commerce")
public class Commerce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du commerce est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255)
    @Column(nullable = false)
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String ville;

    @Size(max = 20)
    @Column(length = 20)
    private String telephone;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categorie;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietaire_id", nullable = false, unique = true)
    private Utilisateur proprietaire;

    public Commerce() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Utilisateur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.proprietaire = proprietaire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commerce other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Commerce[id=" + id + "]";
    }
}
