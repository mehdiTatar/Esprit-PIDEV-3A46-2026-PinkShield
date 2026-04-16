package tn.esprit.services;

import tn.esprit.entities.Parapharmacy;
import tn.esprit.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParapharmacyService {
    private Connection conn;

    public ParapharmacyService() {
        conn = MyDB.getInstance().getConnection();
    }

    public boolean addProduct(Parapharmacy product) {
        String query = "INSERT INTO parapharmacy (name, description, price, stock, category, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Parapharmacy> getAllProducts() {
        List<Parapharmacy> list = new ArrayList<>();
        String query = "SELECT * FROM parapharmacy ORDER BY name ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Parapharmacy(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getString("image_path")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateProduct(Parapharmacy product) {
        String query = "UPDATE parapharmacy SET name = ?, description = ?, price = ?, stock = ?, category = ?, image_path = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImagePath());
            ps.setInt(7, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        String query = "DELETE FROM parapharmacy WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Parapharmacy> searchProducts(String name) {
        List<Parapharmacy> list = new ArrayList<>();
        String query = "SELECT * FROM parapharmacy WHERE name LIKE ? OR category LIKE ? ORDER BY name ASC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + name + "%");
            ps.setString(2, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Parapharmacy(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("image_path")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean productExists(Parapharmacy product, Integer excludeId) {
        String query = "SELECT COUNT(*) FROM parapharmacy WHERE LOWER(name) = LOWER(?) AND LOWER(category) = LOWER(?) AND id <> ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setInt(3, excludeId == null ? -1 : excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
