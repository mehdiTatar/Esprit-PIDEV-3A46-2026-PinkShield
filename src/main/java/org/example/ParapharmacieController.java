package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

public class ParapharmacieController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrix;
    @FXML private TextField txtStock;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane productFlowPane;

    private ServiceParapharmacie service;
    private ServiceWishlist wishlistService;
    private ObservableList<Parapharmacie> productList;
    private FilteredList<Parapharmacie> filteredList;

    @FXML
    public void initialize() {
        service = new ServiceParapharmacie();
        wishlistService = new ServiceWishlist();

        // Initialisation des options de tri
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Nom (A-Z)", "Nom (Z-A)", "Prix croissant", "Prix décroissant"
            ));
        }

        loadProducts();
        setupSearchAndSort();
    }

    private void loadProducts() {
        try {
            ArrayList<Parapharmacie> products = service.afficherAll();
            productList = FXCollections.observableArrayList(products);
            System.out.println("Loaded " + products.size() + " products");

            // On initialise la liste filtrée ici pour éviter les NullPointerException
            if (filteredList == null) {
                filteredList = new FilteredList<>(productList, p -> true);
            } else {
                // Si on refresh, on remet à jour la source
                setupSearchAndSort();
            }

            updateFilterAndSort(); // Première passe d'affichage
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            productList = FXCollections.observableArrayList();
            displayProducts(productList);
        }
    }

    private void setupSearchAndSort() {
        if (productList == null) return;
        filteredList = new FilteredList<>(productList, p -> true);

        // Recherche en temps réel
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());

        // Tri en temps réel
        if (sortComboBox != null) {
            sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());
        }
    }

    private void updateFilterAndSort() {
        if (productList == null) return;

        // 1. Filtrage par recherche
        String searchText = searchBar.getText() != null ? searchBar.getText().toLowerCase() : "";
        filteredList.setPredicate(product -> {
            if (searchText.isEmpty()) return true;
            return product.getNom().toLowerCase().contains(searchText);
        });

        // 2. Tri
        ObservableList<Parapharmacie> sortedList = FXCollections.observableArrayList(filteredList);
        String sortOption = sortComboBox.getValue();

        if (sortOption != null) {
            switch (sortOption) {
                case "Nom (A-Z)":
                    sortedList.sort(Comparator.comparing(p -> p.getNom().toLowerCase()));
                    break;
                case "Nom (Z-A)":
                    sortedList.sort((p1, p2) -> p2.getNom().toLowerCase().compareTo(p1.getNom().toLowerCase()));
                    break;
                case "Prix croissant":
                    sortedList.sort(Comparator.comparingDouble(Parapharmacie::getPrix));
                    break;
                case "Prix décroissant":
                    sortedList.sort((p1, p2) -> Double.compare(p2.getPrix(), p1.getPrix()));
                    break;
            }
        }

        // 3. Affichage
        displayProducts(sortedList);
    }

    private void displayProducts(ObservableList<Parapharmacie> products) {
        productFlowPane.getChildren().clear();
        for (Parapharmacie product : products) {
            VBox card = createProductCard(product);
            productFlowPane.getChildren().add(card);
        }
    }

    private VBox createProductCard(Parapharmacie product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(280);

        Label nameLabel = new Label("📦 " + product.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label("💰 Price: $" + String.format("%.2f", product.getPrix()));
        Label stockLabel = new Label("📊 Stock: " + product.getStock());

        Button wishlistButton = new Button("❤️ Add to Wishlist");
        wishlistButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 5; -fx-padding: 8 15;");
        wishlistButton.setOnAction(e -> addToWishlist(product));
        wishlistButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, wishlistButton);
        VBox.setMargin(wishlistButton, new Insets(10, 0, 0, 0));
        return card;
    }

    private void addToWishlist(Parapharmacie product) {
        try {
            if (wishlistService.wishlistItemExists(1, product.getId())) {
                showWarningAlert("Already in Wishlist", "This product is already in your wishlist.");
                return;
            }
            wishlistService.ajouter(new Wishlist(1, product.getId()));
            showInfoAlert("Success", "Product added to wishlist!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", e.getMessage());
        }
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) return;
        try {
            service.ajouter(new Parapharmacie(txtNom.getText(), Double.parseDouble(txtPrix.getText()), Integer.parseInt(txtStock.getText()), ""));
            showInfoAlert("Success", "Product added to wishlist!");
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    @FXML public void handleRefresh() { loadProducts(); }

    private boolean validateInput() {
        return !txtNom.getText().isEmpty() && !txtPrix.getText().isEmpty() && !txtStock.getText().isEmpty();
    }

    private void clearFields() {
        txtNom.clear(); txtPrix.clear(); txtStock.clear();
    }

    private void showWarningAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
    private void showInfoAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
}