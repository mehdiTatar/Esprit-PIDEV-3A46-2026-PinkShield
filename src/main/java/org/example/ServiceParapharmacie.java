package org.example;

import java.sql.*;
import java.util.ArrayList;

public class ServiceParapharmacie {

    private Connection cnx;

    public ServiceParapharmacie() {
        try {
            String url = "jdbc:mysql://localhost:3306/pinkshield_db";
            String user = "root";
            String password = "";

            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion a la base 'pinkshield_db' reussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public boolean productExists(String nom) throws SQLException {
        String req = "SELECT COUNT(*) as count FROM parapharmacie WHERE nom = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, nom);

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }

    public void ajouter(Parapharmacie parapharmacie) throws SQLException {
        String req = "INSERT INTO parapharmacie (nom, prix, stock, description) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, parapharmacie.getNom());
        pst.setDouble(2, parapharmacie.getPrix());
        pst.setInt(3, parapharmacie.getStock());
        pst.setString(4, parapharmacie.getDescription());

        pst.executeUpdate();
        System.out.println("Produit ajoute avec succes !");
    }

    public void modifier(Parapharmacie parapharmacie) throws SQLException {
        String req = "UPDATE parapharmacie SET nom = ?, prix = ?, stock = ?, description = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setString(1, parapharmacie.getNom());
        pst.setDouble(2, parapharmacie.getPrix());
        pst.setInt(3, parapharmacie.getStock());
        pst.setString(4, parapharmacie.getDescription());
        pst.setInt(5, parapharmacie.getId());

        pst.executeUpdate();
        System.out.println("Produit modifie avec succes !");
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM parapharmacie WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setInt(1, id);

        pst.executeUpdate();
        System.out.println("Produit supprime avec succes !");
    }

    public ArrayList<Parapharmacie> afficherAll() throws SQLException {
        ArrayList<Parapharmacie> list = new ArrayList<>();
        String req = "SELECT * FROM parapharmacie";
        PreparedStatement pst = cnx.prepareStatement(req);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Parapharmacie parapharmacie = new Parapharmacie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("prix"),
                    rs.getInt("stock"),
                    rs.getString("description")
            );
            list.add(parapharmacie);
        }
        return list;
    }
}

