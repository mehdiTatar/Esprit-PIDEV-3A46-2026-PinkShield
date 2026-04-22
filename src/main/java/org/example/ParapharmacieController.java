package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

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
    private ObservableList<Parapharmacie> productList;
    private FilteredList<Parapharmacie> filteredList;
    private Parapharmacie selectedProduct;

    @FXML
    public void initialize() {
        service = new ServiceParapharmacie();

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

            filteredList = new FilteredList<>(productList, p -> true);

            updateFilterAndSort(); // Première passe d'affichage
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
            productList = FXCollections.observableArrayList();
            filteredList = new FilteredList<>(productList, p -> true);
            displayProducts(productList);
        }
    }

    private void setupSearchAndSort() {
        if (filteredList == null && productList != null) {
            filteredList = new FilteredList<>(productList, p -> true);
        }

        // Recherche en temps réel
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());

        // Tri en temps réel
        if (sortComboBox != null) {
            sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilterAndSort());
        }
    }

    private void updateFilterAndSort() {
        if (productList == null || filteredList == null) return;

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

        if (selectedProduct != null && selectedProduct.getId() == product.getId()) {
            card.setStyle("-fx-background-color: #eef7ff; -fx-border-color: #0984e3; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(9,132,227,0.2), 6, 0, 0, 2);");
        }

        Label nameLabel = new Label("📦 " + product.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label("💰 Price: " + String.format("%.2f TND", product.getPrix()));
        Label stockLabel = new Label("📊 Stock: " + product.getStock());
        Label descLabel = new Label("📝 " + (product.getDescription() == null || product.getDescription().isBlank() ? "No description" : product.getDescription()));
        descLabel.setWrapText(true);

        HBox cardActions = new HBox(8);
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 12;");
        editButton.setOnAction(e -> selectProduct(product));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12;");
        deleteButton.setOnAction(e -> deleteProduct(product));

        cardActions.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, descLabel, cardActions);
        VBox.setMargin(cardActions, new Insets(10, 0, 0, 0));
        card.setOnMouseClicked(e -> selectProduct(product));
        return card;
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) return;
        try {
            if (selectedProduct == null && service.productExists(txtNom.getText().trim())) {
                showWarningAlert("Duplicate", "This product already exists.");
                return;
            }

            if (selectedProduct == null) {
                Parapharmacie product = new Parapharmacie(
                        txtNom.getText().trim(),
                        Double.parseDouble(txtPrix.getText().trim()),
                        Integer.parseInt(txtStock.getText().trim()),
                        ""
                );
                service.ajouter(product);
                showInfoAlert("Success", "Product added successfully.");
            } else {
                selectedProduct.setNom(txtNom.getText().trim());
                selectedProduct.setPrix(Double.parseDouble(txtPrix.getText().trim()));
                selectedProduct.setStock(Integer.parseInt(txtStock.getText().trim()));
                service.modifier(selectedProduct);
                showInfoAlert("Success", "Product updated successfully.");
            }

            selectedProduct = null;
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    private void selectProduct(Parapharmacie product) {
        selectedProduct = product;
        txtNom.setText(product.getNom());
        txtPrix.setText(String.valueOf(product.getPrix()));
        txtStock.setText(String.valueOf(product.getStock()));
        displayProducts(FXCollections.observableArrayList(filteredList));
        showInfoAlert("Edit Mode", "You are editing '" + product.getNom() + "'. Click Ajouter / Sauver to apply changes.");
    }

    private void deleteProduct(Parapharmacie product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete product");
        confirm.setContentText("Delete '" + product.getNom() + "' from parapharmacie?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            service.supprimer(product.getId());
            if (selectedProduct != null && selectedProduct.getId() == product.getId()) {
                selectedProduct = null;
                clearFields();
            }
            loadProducts();
            showInfoAlert("Deleted", "Product deleted successfully.");
        } catch (Exception e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        selectedProduct = null;
        clearFields();
        loadProducts();
    }

    private boolean validateInput() {
        if (txtNom.getText().trim().isEmpty() || txtPrix.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty()) {
            showWarningAlert("Validation", "Name, price and stock are required.");
            return false;
        }
        try {
            double price = Double.parseDouble(txtPrix.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (price < 0 || stock < 0) {
                showWarningAlert("Validation", "Price and stock must be non-negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Validation", "Price and stock must be numeric.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtNom.clear();
        txtPrix.clear();
        txtStock.clear();
    }

    private void showWarningAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
    private void showInfoAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(c); a.showAndWait(); }
}