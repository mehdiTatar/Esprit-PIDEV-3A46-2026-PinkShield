package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class ServiceWishlist {

    private Connection cnx;

    public ServiceWishlist() {
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

    public boolean wishlistItemExists(int user_id, int parapharmacie_id) throws SQLException {
        String req = "SELECT COUNT(*) as count FROM wishlist WHERE user_id = ? AND parapharmacie_id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, user_id);
        pst.setInt(2, parapharmacie_id);

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }

    public void ajouter(Wishlist wishlist) throws SQLException {
        if (wishlist == null || wishlist.getUser_id() <= 0) {
            throw new SQLException("User session is required to add wishlist items.");
        }
        String req = "INSERT INTO wishlist (user_id, parapharmacie_id, added_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, wishlist.getUser_id());
            pst.setInt(2, wishlist.getParapharmacie_id());
            pst.executeUpdate();
        }
        System.out.println("Wishlist item ajoute avec succes !");
    }

    public boolean add(Product product) throws SQLException {
        if (product == null || product.getName() == null || product.getName().isBlank()) {
            return false;
        }

        ServiceParapharmacie parapharmacieService = new ServiceParapharmacie();
        Parapharmacie dbProduct = findMatchingParapharmacie(parapharmacieService, product.getName());
        if (dbProduct == null) {
            dbProduct = createParapharmacieFromAiProduct(parapharmacieService, product);
        }
        if (dbProduct == null) {
            System.out.println("Product not found in Parapharmacie inventory: " + product.getName());
            return false;
        }

        int currentUserId = getCurrentUserIdOrThrow();
        if (!wishlistItemExists(currentUserId, dbProduct.getId())) {
            ajouter(new Wishlist(currentUserId, dbProduct.getId()));
        }

        return true;
    }

    private Parapharmacie findMatchingParapharmacie(ServiceParapharmacie parapharmacieService, String productName) throws SQLException {
        Parapharmacie exactMatch = parapharmacieService.findByName(productName);
        if (exactMatch != null) {
            return exactMatch;
        }

        String normalizedTarget = normalizeName(productName);
        for (Parapharmacie candidate : parapharmacieService.afficherAll()) {
            if (normalizeName(candidate.getNom()).equals(normalizedTarget)) {
                return candidate;
            }
        }
        return null;
    }

    private Parapharmacie createParapharmacieFromAiProduct(ServiceParapharmacie parapharmacieService, Product product) throws SQLException {
        Parapharmacie newProduct = new Parapharmacie();
        newProduct.setNom(product.getName().trim());
        newProduct.setDescription(product.getCategory() == null ? "AI recommended product" : product.getCategory());
        newProduct.setPrix(product.getPrice());
        newProduct.setStock(1);

        parapharmacieService.ajouter(newProduct);
        return findMatchingParapharmacie(parapharmacieService, product.getName());
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "")
                .trim();
    }

    public void modifier(Wishlist wishlist) throws SQLException {
        String req = "UPDATE wishlist SET user_id = ?, parapharmacie_id = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);

        pst.setInt(1, wishlist.getUser_id());
        pst.setInt(2, wishlist.getParapharmacie_id());
        pst.setInt(3, wishlist.getId());

        pst.executeUpdate();
        System.out.println("Wishlist item modifie avec succes !");
    }

    public void supprimer(int id) throws SQLException {
        UserSession session = UserSession.getInstance();
        boolean scopeToCurrentUser = session.isLoggedIn() && !session.isAdmin();
        String req = "DELETE FROM wishlist WHERE id = ?" + (scopeToCurrentUser ? " AND user_id = ?" : "");
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            if (scopeToCurrentUser) {
                pst.setInt(2, session.getUserId());
            }
            pst.executeUpdate();
        }
        System.out.println("Wishlist item supprime avec succes !");
    }

    public ArrayList<Wishlist> getByUserId(int userId) throws SQLException {
        ArrayList<Wishlist> list = new ArrayList<>();
        if (userId <= 0) {
            return list;
        }

        String req = "SELECT * FROM wishlist WHERE user_id = ? ORDER BY added_at DESC";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new Wishlist(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("parapharmacie_id"),
                            rs.getTimestamp("added_at")
                    ));
                }
            }
        }
        return list;
    }

    public ArrayList<Wishlist> afficherAll() throws SQLException {
        ArrayList<Wishlist> list = new ArrayList<>();
        String req = "SELECT * FROM wishlist";
        PreparedStatement pst = cnx.prepareStatement(req);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Wishlist wishlist = new Wishlist(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("parapharmacie_id"),
                    rs.getTimestamp("added_at")
            );
            list.add(wishlist);
        }
        return list;
    }

    private int getCurrentUserIdOrThrow() throws SQLException {
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            throw new SQLException("No active user session.");
        }
        return session.getUserId();
    }
}

