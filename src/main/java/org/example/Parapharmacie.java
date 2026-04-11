package org.example;

public class Parapharmacie {
    private int id;
    private String nom;
    private double prix;
    private int stock;
    private String description;

    public Parapharmacie() {
    }

    public Parapharmacie(String nom, double prix, int stock, String description) {
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
    }

    public Parapharmacie(int id, String nom, double prix, int stock, String description) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Parapharmacie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                '}';
    }
}

