package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.*;
import org.h2.tools.Server;

public class App {
    public static void main(String[] args) throws Exception {
        Server serveur = Server.createWebServer("-webPort", "8082").start();
        EntityManagerFactory emf = null;
        try {
            emf = Persistence.createEntityManagerFactory("tp-hibernate");
            insererDonnees(emf);
            lireProduits(emf);

            // Recuperation des vrais identifiants generes, sans supposer 1 ou 2.
            EntityManager em = emf.createEntityManager();
            List<Long> ids;
            try {
                ids = em.createQuery("select p.id from Produit p order by p.id", Long.class)
                        .getResultList();
            } finally {
                em.close();
            }

            mettreAJourPrixProduit(emf, ids.get(0), new BigDecimal("899.90"));
            rechercherParPlageDePrix(emf, new BigDecimal("20.00"), new BigDecimal("900.00"));
            supprimerProduit(emf, ids.get(1));
            lireProduits(emf);

            System.out.println("Console H2 : http://localhost:8082");
            System.out.println("URL JDBC : jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            System.out.println("Utilisateur : sa ; mot de passe vide.");
            System.out.println("Appuyez sur Entree pour fermer l'application.");
            System.in.read(); // Maintient la JVM et la base en memoire accessibles.
        } finally {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            serveur.stop();
        }
    }

    // Centralise transaction, rollback en cas d'erreur et fermeture de l'EntityManager.
    private static void dansTransaction(EntityManagerFactory emf, Consumer<EntityManager> action) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            action.accept(em);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static void insererDonnees(EntityManagerFactory emf) {
        dansTransaction(emf, em -> {
            Categorie informatique = new Categorie("Informatique");
            Categorie livres = new Categorie("Livres");
            em.persist(informatique);
            em.persist(livres);
            em.persist(new Produit("Ordinateur", new BigDecimal("999.90"), informatique));
            em.persist(new Produit("Souris", new BigDecimal("25.50"), informatique));
            em.persist(new Produit("Java et JPA", new BigDecimal("39.90"), livres));
        });
    }

    public static void lireProduits(EntityManagerFactory emf) {
        dansTransaction(emf, em -> {
            System.out.println("--- Tous les produits ---");
            em.createQuery("select p from Produit p join fetch p.categorie order by p.id", Produit.class)
                    .getResultList().forEach(System.out::println);
        });
    }

    // Exercice : find retourne une entite geree. Hibernate detecte la modification
    // du prix et execute l'UPDATE au commit ; aucun appel a merge n'est necessaire.
    public static void mettreAJourPrixProduit(EntityManagerFactory emf, Long produitId,
                                              BigDecimal nouveauPrix) {
        if (nouveauPrix == null || nouveauPrix.signum() < 0) {
            throw new IllegalArgumentException("Le nouveau prix doit etre positif ou nul.");
        }
        dansTransaction(emf, em -> {
            Produit produit = em.find(Produit.class, produitId);
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable : " + produitId);
            }
            produit.setPrix(nouveauPrix);
        });
    }

    // Exercice : remove exige une entite geree, obtenue ici par find.
    // La categorie reste conservee : aucune suppression en cascade n'est configuree.
    public static void supprimerProduit(EntityManagerFactory emf, Long produitId) {
        dansTransaction(emf, em -> {
            Produit produit = em.find(Produit.class, produitId);
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable : " + produitId);
            }
            em.remove(produit);
        });
    }

    // Exercice : BETWEEN inclut les deux bornes. Les parametres nommes evitent
    // de concatener les valeurs dans JPQL. JOIN FETCH charge aussi la categorie.
    public static List<Produit> rechercherParPlageDePrix(EntityManagerFactory emf,
                                                        BigDecimal prixMin, BigDecimal prixMax) {
        if (prixMin == null || prixMax == null || prixMin.compareTo(prixMax) > 0) {
            throw new IllegalArgumentException("Les bornes doivent verifier prixMin <= prixMax.");
        }
        EntityManager em = emf.createEntityManager();
        try {
            List<Produit> produits = em.createQuery(
                    "select p from Produit p join fetch p.categorie "
                            + "where p.prix between :min and :max order by p.prix", Produit.class)
                    .setParameter("min", prixMin)
                    .setParameter("max", prixMax)
                    .getResultList();
            System.out.println("--- Produits entre " + prixMin + " et " + prixMax + " ---");
            produits.forEach(System.out::println);
            return produits;
        } finally {
            em.close();
        }
    }
}
