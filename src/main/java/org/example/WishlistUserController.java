package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WishlistUserController {

    @FXML
    private TextField searchBar;
    @FXML
    private TableView<WishlistDisplayItem> table;
    @FXML
    private TableColumn<WishlistDisplayItem, Integer> colId;
    @FXML
    private TableColumn<WishlistDisplayItem, String> colParapharmacieId;
    @FXML
    private TableColumn<WishlistDisplayItem, String> colPrice;
    @FXML
    private TableColumn<WishlistDisplayItem, String> colAddedAt;
    @FXML
    private TableColumn<WishlistDisplayItem, String> colAction;
    @FXML
    private Label totalPriceLabel;

    private ServiceWishlist serviceWishlist;
    private ServiceParapharmacie serviceParapharmacie;
    private ObservableList<WishlistDisplayItem> wishlistList;
    private FilteredList<WishlistDisplayItem> filteredList;

    @FXML
    public void initialize() {
        serviceWishlist = new ServiceWishlist();
        serviceParapharmacie = new ServiceParapharmacie();

        initializeTableColumns();
        loadWishlist();
        setupRealTimeSearch();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colParapharmacieId.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatPrice(cellData.getValue().getPrice())));

        colAddedAt.setCellValueFactory(cellData -> {
            java.sql.Timestamp timestamp = cellData.getValue().getAddedAt();
            String formatted = timestamp != null ?
                timestamp.toLocalDateTime().toLocalDate().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        colAction.setCellFactory(column -> new TableCell<WishlistDisplayItem, String>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                deleteButton.setOnAction(event -> {
                    WishlistDisplayItem item = getTableView().getItems().get(getIndex());
                    handleDelete(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void loadWishlist() {
        try {
            UserSession session = UserSession.getInstance();
            if (!session.isLoggedIn()) {
                wishlistList = FXCollections.observableArrayList();
                filteredList = new FilteredList<>(wishlistList, p -> true);
                table.setItems(filteredList);
                updateTotalPrice();
                showWarningAlert("Authentication Required", "Please sign in to view your wishlist.");
                return;
            }

            ArrayList<Wishlist> wishlists = serviceWishlist.getByUserId(session.getUserId());

            Map<Integer, Parapharmacie> productById = loadProductMap();
            ArrayList<WishlistDisplayItem> displayItems = new ArrayList<>();
            for (Wishlist wishlist : wishlists) {
                Parapharmacie product = productById.get(wishlist.getParapharmacie_id());
                String productName = product != null ? product.getNom() : "Product #" + wishlist.getParapharmacie_id();
                double price = product != null ? product.getPrix() : 0.0;

                displayItems.add(new WishlistDisplayItem(
                        wishlist.getId(),
                        wishlist.getUser_id(),
                        wishlist.getParapharmacie_id(),
                        productName,
                        price,
                        wishlist.getAdded_at()
                ));
            }

            wishlistList = FXCollections.observableArrayList(displayItems);
            filteredList = new FilteredList<>(wishlistList, p -> true);
            table.setItems(filteredList);
            updateTotalPrice();
        } catch (SQLException e) {
            System.out.println("Database connection error - showing empty wishlist: " + e.getMessage());
            wishlistList = FXCollections.observableArrayList();
            filteredList = new FilteredList<>(wishlistList, p -> true);
            table.setItems(filteredList);
            updateTotalPrice();
        } catch (NullPointerException e) {
            System.out.println("Database not ready - showing empty wishlist: " + e.getMessage());
            wishlistList = FXCollections.observableArrayList();
            filteredList = new FilteredList<>(wishlistList, p -> true);
            table.setItems(filteredList);
            updateTotalPrice();
        }
    }

    private Map<Integer, Parapharmacie> loadProductMap() throws SQLException {
        ArrayList<Parapharmacie> products = serviceParapharmacie.afficherAll();
        Map<Integer, Parapharmacie> productById = new HashMap<>();
        for (Parapharmacie product : products) {
            productById.put(product.getId(), product);
        }
        return productById;
    }

    private void setupRealTimeSearch() {
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredList == null) {
                return;
            }

            filteredList.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String productName = item.getProductName() != null ? item.getProductName().toLowerCase() : "";
                String id = String.valueOf(item.getId());
                String price = formatPrice(item.getPrice()).toLowerCase();

                return productName.toLowerCase().contains(lowerCaseFilter) ||
                       id.contains(lowerCaseFilter) ||
                       price.contains(lowerCaseFilter);
            });

            updateTotalPrice();
        });
    }

    @FXML
    public void handleSupprimer() {
        WishlistDisplayItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a wishlist item to remove.");
            return;
        }

        handleDelete(selected);
    }

    private void handleDelete(WishlistDisplayItem item) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showWarningAlert("Authentication Required", "Please sign in to perform this action.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Item");
        confirmation.setHeaderText("Remove from Wishlist");
        confirmation.setContentText("Are you sure you want to remove this item from your wishlist?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceWishlist.supprimer(item.getId());
                    showInfoAlert("Success", "Item removed from wishlist!");
                    loadWishlist();
                } catch (SQLException e) {
                    showErrorAlert("Database Error", "Could not remove item: " + e.getMessage());
                } catch (Exception e) {
                    showErrorAlert("Error", "Could not remove item: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleRefresh() {
        loadWishlist();
        showInfoAlert("Refreshed", "Wishlist reloaded!");
    }

    private void updateTotalPrice() {
        if (totalPriceLabel == null || filteredList == null) {
            return;
        }

        double total = 0.0;
        for (WishlistDisplayItem item : filteredList) {
            total += item.getPrice();
        }

        totalPriceLabel.setText("Total: " + formatPrice(total));
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f TND", price);
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
