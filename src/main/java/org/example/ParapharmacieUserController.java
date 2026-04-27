package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

// Change la ligne 16 par :
public class ParapharmacieUserController {

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

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)", "Nom (Z-A)", "Prix croissant", "Prix décroissant"
        ));

        loadProducts();
        setupSearchAndSort();
    }

    private void loadProducts() {
        try {
            ArrayList<Parapharmacie> products = service.afficherAll();
            productList = FXCollections.observableArrayList(products);
            filteredList = new FilteredList<>(productList, p -> true);
            updateFilterAndSort();
        } catch (Exception e) {
            System.err.println("Error loading parapharmacie products: " + e.getMessage());
            productList = FXCollections.observableArrayList();
            filteredList = new FilteredList<>(productList, p -> true);
            displayProducts(productList);
        }
    }

    private void setupSearchAndSort() {
        if (filteredList == null && productList != null) {
            filteredList = new FilteredList<>(productList, p -> true);
        }

        searchBar.textProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());
    }

    private void updateFilterAndSort() {
        if (productList == null || filteredList == null) return;

        String searchText = searchBar != null && searchBar.getText() != null ? searchBar.getText().toLowerCase() : "";
        filteredList.setPredicate(product -> {
            if (searchText.isEmpty()) return true;
            return product.getNom().toLowerCase().contains(searchText);
        });

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
        card.getStyleClass().add("para-product-card");
        card.setPrefWidth(280);

        Label nameLabel = new Label("📦 " + product.getNom());
        nameLabel.getStyleClass().add("para-product-name");

        Label priceLabel = new Label("💰 Price: " + String.format("%.2f TND", product.getPrix()));
        priceLabel.getStyleClass().add("para-product-meta");
        Label stockLabel = new Label("📊 Stock: " + product.getStock());
        stockLabel.getStyleClass().add("para-product-meta");

        Button wishlistButton = new Button("Add to Wishlist");
        wishlistButton.getStyleClass().addAll("btn-primary", "para-wishlist-button");
        wishlistButton.setGraphic(new FontIcon("fas-heart"));
        wishlistButton.getGraphic().getStyleClass().add("sidebar-icon");
        wishlistButton.setGraphicTextGap(15);
        wishlistButton.setOnAction(e -> addToWishlist(product));
        wishlistButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, wishlistButton);
        return card;
    }

    private void addToWishlist(Parapharmacie product) {
        try {
            UserSession session = UserSession.getInstance();
            if (!session.isLoggedIn()) {
                showWarningAlert("Authentication Required", "Please sign in to add items to wishlist.");
                return;
            }

            if (wishlistService.wishlistItemExists(session.getUserId(), product.getId())) {
                showWarningAlert("Already in Wishlist", "This product is already in your wishlist.");
                return;
            }
            wishlistService.ajouter(new Wishlist(session.getUserId(), product.getId()));
            showInfoAlert("Success", "Product added to wishlist!");
        } catch (SQLException e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) return;
        try {
            if (service.productExists(txtNom.getText())) {
                showErrorAlert("Duplicate", "Product already exists.");
                return;
            }
            service.ajouter(new Parapharmacie(txtNom.getText(), Double.parseDouble(txtPrix.getText()), Integer.parseInt(txtStock.getText()), ""));
            showInfoAlert("Success", "Added!");
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