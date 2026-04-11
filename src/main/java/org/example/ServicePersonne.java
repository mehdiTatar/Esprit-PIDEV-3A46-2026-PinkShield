package org.example; // Laisse bien ta ligne de package si elle est différente

import java.sql.*;
import java.util.ArrayList;

public class ServicePersonne {

    // Variable pour garder la connexion ouverte
    private Connection cnx;

    // 1. Le constructeur : Il se connecte à MySQL dès qu'on appelle le service
    public ServicePersonne() {
        try {
            // Paramètres de connexion demandés dans le TP
            String url = "jdbc:mysql://127.0.0.1:3306/pinkshield_db?serverVersion=8.0&charset=utf8mb4";
            String user = "root";
            String password = ""; // Vide par défaut sous XAMPP/WAMP

            cnx = DriverManager.getConnection(url, user, password);
            System.out.println(" Connexion à la base 'pinkshield_db' réussie !");
        } catch (SQLException e) {
            System.out.println(" Erreur de connexion : " + e.getMessage());
        }
    }

    // 2. Méthode pour AJOUTER (Insert)
    public void ajouter(Personne per) throws SQLException {
        // Les "?" sont des paramètres sécurisés remplis par le PreparedStatement
        String req = "INSERT INTO personne (nom, prenom) VALUES (?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, per.getNom());
        pst.setString(2, per.getPrenom());

        pst.executeUpdate(); // executeUpdate() pour Insert, Update, Delete
        System.out.println("👤 Personne ajoutée avec succès !");
    }

    // 3. Méthode pour MODIFIER le prénom (Update)
    public void updatePrenom(Personne per) throws SQLException {
        String req = "UPDATE personne SET prenom = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, per.getPrenom());
        pst.setInt(2, per.getId());

        pst.executeUpdate();
        System.out.println("✏️ Prénom mis à jour avec succès !");
    }

    // 4. Méthode pour SUPPRIMER (Delete)
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM personne WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setInt(1, id);

        pst.executeUpdate();
        System.out.println("🗑️ Personne supprimée avec succès !");
    }

    // 5. Méthode pour AFFICHER TOUT (Select)
    public ArrayList<Personne> afficherAll() throws SQLException {
        ArrayList<Personne> list = new ArrayList<>();
        String req = "SELECT * FROM personne";
        PreparedStatement pst = cnx.prepareStatement(req);

        ResultSet rs = pst.executeQuery(); // executeQuery() renvoie un ResultSet

        while (rs.next()) {
            // On récupère les données de MySQL pour créer un objet Personne
            Personne p = new Personne(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom")
            );
            list.add(p); // On ajoute la personne à la liste
        }
        return list;
    }
}