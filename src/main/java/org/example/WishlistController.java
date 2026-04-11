package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class WishlistController {

    @FXML
    private TextField txtUserId;
    @FXML
    private TextField txtParapharmacieId;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Wishlist> table;
    @FXML
    private TableColumn<Wishlist, Integer> colId;
    @FXML
    private TableColumn<Wishlist, Integer> colUserId;
    @FXML
    private TableColumn<Wishlist, Integer> colParapharmacieId;
    @FXML
    private TableColumn<Wishlist, java.sql.Timestamp> colAddedAt;

    private ServiceWishlist service;
    private ObservableList<Wishlist> wishlistList;
    private FilteredList<Wishlist> filteredList;

    @FXML
    public void initialize() {
        service = new ServiceWishlist();

        initializeTableColumns();
        loadWishlists();
        setupRealTimeSearch();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        colParapharmacieId.setCellValueFactory(new PropertyValueFactory<>("parapharmacie_id"));
        colAddedAt.setCellValueFactory(new PropertyValueFactory<>("added_at"));
    }

    private void loadWishlists() {
        try {
            ArrayList<Wishlist> wishlists = service.afficherAll();
            wishlistList = FXCollections.observableArrayList(wishlists);
            table.setItems(wishlistList);
        } catch (SQLException e) {
            System.out.println("Database connection error - this is OK for testing: " + e.getMessage());
            wishlistList = FXCollections.observableArrayList();
            table.setItems(wishlistList);
        } catch (NullPointerException e) {
            System.out.println("Database not ready - this is OK: " + e.getMessage());
            wishlistList = FXCollections.observableArrayList();
            table.setItems(wishlistList);
        }
    }

    private void setupRealTimeSearch() {
        filteredList = new FilteredList<>(wishlistList, p -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(wishlist -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String userId = String.valueOf(wishlist.getUser_id());
                String parapharmacieId = String.valueOf(wishlist.getParapharmacie_id());
                String id = String.valueOf(wishlist.getId());

                return userId.contains(lowerCaseFilter) ||
                       parapharmacieId.contains(lowerCaseFilter) ||
                       id.contains(lowerCaseFilter);
            });
        });

        table.setItems(filteredList);
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) {
            return;
        }

        try {
            int userId = Integer.parseInt(txtUserId.getText());
            int parapharmacieId = Integer.parseInt(txtParapharmacieId.getText());

            if (service.wishlistItemExists(userId, parapharmacieId)) {
                showErrorAlert("Duplicate Item", "This product (ID: " + parapharmacieId + ") is already in user " + userId + "'s wishlist.");
                return;
            }

            Wishlist wishlist = new Wishlist(userId, parapharmacieId);
            service.ajouter(wishlist);
            showInfoAlert("Success", "Wishlist item added successfully!");

            clearFields();
            loadWishlists();

        } catch (NumberFormatException e) {
            showErrorAlert("Input Error", "User ID and Parapharmacie ID must be valid integers.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not add wishlist item: " + e.getMessage());
        }
    }

    @FXML
    public void handleSupprimer() {
        Wishlist selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a wishlist item to delete.");
            return;
        }

        try {
            service.supprimer(selected.getId());
            showInfoAlert("Success", "Wishlist item deleted successfully!");
            loadWishlists();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not delete wishlist item: " + e.getMessage());
        }
    }


    private boolean validateInput() {
        String userId = txtUserId.getText();
        String parapharmacieId = txtParapharmacieId.getText();

        if (userId.isEmpty() || parapharmacieId.isEmpty()) {
            showWarningAlert("Validation Error", "All fields are required. Please fill in all fields.");
            return false;
        }

        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "User ID must be a valid integer.");
            return false;
        }

        try {
            Integer.parseInt(parapharmacieId);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Parapharmacie ID must be a valid integer.");
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtUserId.clear();
        txtParapharmacieId.clear();
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

