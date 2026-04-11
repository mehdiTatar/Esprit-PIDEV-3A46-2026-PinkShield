package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class ParapharmacieController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrix;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Parapharmacie> tableParapharmacie;
    @FXML
    private TableColumn<Parapharmacie, Integer> colId;
    @FXML
    private TableColumn<Parapharmacie, String> colNom;
    @FXML
    private TableColumn<Parapharmacie, Double> colPrix;
    @FXML
    private TableColumn<Parapharmacie, Integer> colStock;

    private ServiceParapharmacie service;
    private ObservableList<Parapharmacie> productList;
    private FilteredList<Parapharmacie> filteredList;

    @FXML
    public void initialize() {
        service = new ServiceParapharmacie();

        initializeTableColumns();
        loadProducts();
        setupRealTimeSearch();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void loadProducts() {
        try {
            ArrayList<Parapharmacie> products = service.afficherAll();
            productList = FXCollections.observableArrayList(products);
            tableParapharmacie.setItems(productList);
        } catch (SQLException e) {
            System.out.println("Database connection error - this is OK for testing: " + e.getMessage());
            productList = FXCollections.observableArrayList();
            tableParapharmacie.setItems(productList);
        } catch (NullPointerException e) {
            System.out.println("Database not ready - this is OK: " + e.getMessage());
            productList = FXCollections.observableArrayList();
            tableParapharmacie.setItems(productList);
        }
    }

    private void setupRealTimeSearch() {
        filteredList = new FilteredList<>(productList, p -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return product.getNom().toLowerCase().contains(lowerCaseFilter);
            });
        });

        tableParapharmacie.setItems(filteredList);
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) {
            return;
        }

        try {
            String nom = txtNom.getText();
            double prix = Double.parseDouble(txtPrix.getText());
            int stock = Integer.parseInt(txtStock.getText());
            String description = "";

            if (service.productExists(nom)) {
                showErrorAlert("Duplicate Product", "A product with the name '" + nom + "' already exists in the database.");
                return;
            }

            Parapharmacie parapharmacie = new Parapharmacie(nom, prix, stock, description);
            service.ajouter(parapharmacie);
            showInfoAlert("Success", "Product added successfully!");

            clearFields();
            loadProducts();

        } catch (NumberFormatException e) {
            showErrorAlert("Input Error", "Prix must be a decimal number and Stock must be an integer.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not add product: " + e.getMessage());
        }
    }

    @FXML
    public void handleSupprimer() {
        Parapharmacie selected = tableParapharmacie.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a product to delete.");
            return;
        }

        try {
            service.supprimer(selected.getId());
            showInfoAlert("Success", "Product deleted successfully!");
            loadProducts();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not delete product: " + e.getMessage());
        }
    }


    private boolean validateInput() {
        String nom = txtNom.getText();
        String prix = txtPrix.getText();
        String stock = txtStock.getText();

        if (nom.isEmpty() || prix.isEmpty() || stock.isEmpty()) {
            showWarningAlert("Validation Error", "All fields are required. Please fill in all fields.");
            return false;
        }

        try {
            Double.parseDouble(prix);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Prix must be a valid decimal number.");
            return false;
        }

        try {
            Integer.parseInt(stock);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Stock must be a valid integer.");
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtNom.clear();
        txtPrix.clear();
        txtStock.clear();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

