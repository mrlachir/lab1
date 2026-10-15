package com.example.model;

import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    protected Categorie() { // Constructeur requis par JPA.
    }

    public Categorie(String nom) {
        this.nom = nom;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
