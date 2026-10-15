package com.example.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Entity
@Table(name = "produits")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    protected Produit() { // Constructeur requis par JPA.
    }

    public Produit(String nom, BigDecimal prix, Categorie categorie) {
        this.nom = nom;
        this.prix = prix;
        this.categorie = categorie;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", nom='" + nom + "', prix=" + prix
                + ", categorie='" + categorie.getNom() + "'}";
    }
}
