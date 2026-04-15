package org.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ParapharmacieUserController implements Initializable {

    @FXML
    private TextField searchBar;

    @FXML
    private FlowPane productFlowPane;

    private ServiceParapharmacie serviceParapharmacie;
    private ServiceWishlist serviceWishlist;
    private ArrayList<Parapharmacie> allProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceParapharmacie = new ServiceParapharmacie();
        serviceWishlist = new ServiceWishlist();
        allProducts = new ArrayList<>();

        // Setup search functionality
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });

        loadProducts();
    }

    @FXML
    public void handleRefresh() {
        loadProducts();
    }

    private void loadProducts() {
        try {
            allProducts = serviceParapharmacie.afficherAll();
            if (allProducts.isEmpty()) {
                // Load demo products if database is empty
                loadDemoProducts();
            }
            displayProducts(allProducts);
        } catch (SQLException e) {
            System.out.println("Database connection failed, loading demo products: " + e.getMessage());
            loadDemoProducts();
            displayProducts(allProducts);
        }
    }

    private void loadDemoProducts() {
        allProducts.clear();
        allProducts.add(new Parapharmacie(1, "Aspirin 500mg", 5.99, 150, "Pain relief medication"));
        allProducts.add(new Parapharmacie(2, "Ibuprofen 200mg", 7.50, 120, "Anti-inflammatory medicine"));
        allProducts.add(new Parapharmacie(3, "Paracetamol 500mg", 4.25, 200, "Fever reducer"));
        allProducts.add(new Parapharmacie(4, "Vitamin C 1000mg", 8.99, 100, "Immune booster"));
        allProducts.add(new Parapharmacie(5, "Multivitamin Complex", 12.99, 80, "Daily supplement"));
        allProducts.add(new Parapharmacie(6, "Cough Syrup 200ml", 6.49, 60, "Cough suppressant"));
    }

    private void displayProducts(ArrayList<Parapharmacie> products) {
        productFlowPane.getChildren().clear();

        if (products.isEmpty()) {
            Label noProductsLabel = new Label("No products available at the moment.");
            noProductsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16;");
            productFlowPane.getChildren().add(noProductsLabel);
            return;
        }

        for (Parapharmacie product : products) {
            VBox productCard = createProductCard(product);
            productFlowPane.getChildren().add(productCard);
        }
    }

    private VBox createProductCard(Parapharmacie product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e8e9f3; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(232, 67, 147, 0.1), 8, 0, 0, 3);");
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        card.setAlignment(Pos.TOP_LEFT);

        // Product Name
        Label nameLabel = new Label(product.getNom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#2d3436"));
        nameLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label(String.format("$%.2f", product.getPrix()));
        priceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.web("#e84393"));

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setFont(Font.font("Segoe UI", 12));
        stockLabel.setTextFill(product.getStock() > 0 ? Color.web("#27ae60") : Color.web("#e74c3c"));

        // Description
        Label descLabel = new Label(product.getDescription() != null ? product.getDescription() : "No description available");
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(Color.web("#666"));
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add to Wishlist Button
        Button wishlistBtn = new Button("Add to Wishlist");
        wishlistBtn.setStyle("-fx-background-color: #e84393; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 16; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        wishlistBtn.setOnAction(e -> addToWishlist(product));

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, descLabel, spacer, wishlistBtn);

        return card;
    }

    private void addToWishlist(Parapharmacie product) {
        try {
            // For demo purposes, using user_id = 1 (you might want to implement user sessions)
            Wishlist wishlistItem = new Wishlist(1, product.getId());
            serviceWishlist.ajouter(wishlistItem);

            showAlert("Success", product.getNom() + " added to your wishlist!");
        } catch (SQLException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            System.out.println("DEBUG: Full SQL Error: " + e);
            System.out.println("DEBUG: Error message: " + errorMsg);
            
            // Check if it's a duplicate entry error
            if (errorMsg.contains("Duplicate entry")) {
                showAlert("Info", "This item is already in your wishlist!");
            } else if (errorMsg.contains("Unknown column") || errorMsg.contains("parapharmacie_id")) {
                showAlert("Database Configuration Error", 
                    "The wishlist table needs to be fixed.\n\n" +
                    "Please:\n" +
                    "1. Open PHPMyAdmin or MySQL\n" +
                    "2. Run the FIX_WISHLIST_DATABASE.sql script\n" +
                    "3. Restart the application\n\n" +
                    "Error: " + errorMsg);
            } else if (errorMsg.contains("Table") && errorMsg.contains("doesn't exist")) {
                showAlert("Database Error", 
                    "Wishlist table doesn't exist.\n\n" +
                    "Please run FIX_WISHLIST_DATABASE.sql to create it.");
            } else {
                showAlert("Error", "Failed to add to wishlist:\n" + errorMsg);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to add to wishlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            displayProducts(allProducts);
            return;
        }

        ArrayList<Parapharmacie> filtered = new ArrayList<>();
        String lowerSearch = searchText.toLowerCase();

        for (Parapharmacie product : allProducts) {
            if (product.getNom().toLowerCase().contains(lowerSearch) ||
                (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerSearch))) {
                filtered.add(product);
            }
        }

        displayProducts(filtered);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
