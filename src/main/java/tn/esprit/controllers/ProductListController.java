package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Parapharmacy;
import tn.esprit.entities.User;
import tn.esprit.services.ParapharmacyService;
import tn.esprit.utils.FormValidator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane productsContainer;
    @FXML private Button manageProductsBtn;

    @FXML private Label adminTitle;
    @FXML private Label feedbackLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descArea;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> formCategoryCombo;
    @FXML private TableView<Parapharmacy> productTable;
    @FXML private TableColumn<Parapharmacy, Integer> idCol;
    @FXML private TableColumn<Parapharmacy, String> nameCol;
    @FXML private TableColumn<Parapharmacy, Double> priceCol;
    @FXML private TableColumn<Parapharmacy, Integer> stockCol;
    @FXML private TableColumn<Parapharmacy, String> categoryCol;
    @FXML private TableColumn<Parapharmacy, Void> actionsCol;

    private final ParapharmacyService productService = new ParapharmacyService();
    private final ObservableList<Parapharmacy> allProducts = FXCollections.observableArrayList();
    private User currentUser;
    private Parapharmacy selectedProduct;

    @FXML
    public void initialize() {
        if (productsContainer != null) {
            loadShopData();
            setupShopFilters();
        }

        if (productTable != null) {
            setupAdminTable();
            setupAdminForm();
            loadAdminData();
            FormValidator.attachClearOnInput(feedbackLabel, nameField, priceField, stockField);
            FormValidator.attachClearOnInput(feedbackLabel, formCategoryCombo);
            descArea.textProperty().addListener((obs, oldValue, newValue) -> FormValidator.setMessage(feedbackLabel, "", true));
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getRole().equals("admin") && manageProductsBtn != null) {
            manageProductsBtn.setVisible(true);
            manageProductsBtn.setManaged(true);
        }
    }

    private void loadShopData() {
        allProducts.setAll(productService.getAllProducts());
        renderProductCards(allProducts);
    }

    private void setupShopFilters() {
        ObservableList<String> categories = FXCollections.observableArrayList("All", "Supplements", "Skincare", "Haircare", "Hygiene", "Baby", "Wellness");
        categoryFilter.setItems(categories);
        categoryFilter.setValue("All");

        searchField.textProperty().addListener((obs, oldV, newV) -> filterShop());
        categoryFilter.valueProperty().addListener((obs, oldV, newV) -> filterShop());
    }

    private void filterShop() {
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String cat = categoryFilter.getValue();

        List<Parapharmacy> filtered = allProducts.stream()
                .filter(p -> query.isEmpty()
                        || p.getName().toLowerCase().contains(query)
                        || p.getDescription().toLowerCase().contains(query))
                .filter(p -> cat == null || cat.equals("All") || p.getCategory().equalsIgnoreCase(cat))
                .collect(Collectors.toList());

        renderProductCards(filtered);
    }

    private void renderProductCards(List<Parapharmacy> products) {
        productsContainer.getChildren().clear();
        for (Parapharmacy product : products) {
            productsContainer.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Parapharmacy product) {
        VBox card = new VBox(12);
        card.getStyleClass().add("post-card");
        card.setPrefWidth(230);
        card.setPadding(new javafx.geometry.Insets(18));

        Label category = new Label(product.getCategory().toUpperCase());
        category.getStyleClass().add("auth-status-pill");

        Label name = new Label(product.getName());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);

        Label description = new Label(product.getDescription());
        description.getStyleClass().add("dashboard-copy");
        description.setWrapText(true);
        description.setMaxHeight(70);

        Label price = new Label(String.format("%.2f TND", product.getPrice()));
        price.getStyleClass().add("metric-value");
        price.setStyle("-fx-font-size: 20;");

        Label stock = new Label("Stock: " + product.getStock());
        stock.getStyleClass().add("table-meta");

        Button buyBtn = new Button("Add to cart");
        buyBtn.getStyleClass().add("button");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(category, name, description, price, stock, buyBtn);
        return card;
    }

    @FXML
    public void handleManageProducts() {
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            showAlert("Access denied", "Only administrators can manage products.", Alert.AlertType.WARNING);
            return;
        }
        loadSubView("/fxml/product_admin.fxml");
    }

    private void setupAdminTable() {
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        priceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        stockCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStock()));
        categoryCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));

        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("button");
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 6 10;");
                editBtn.setOnAction(e -> {
                    selectedProduct = getTableView().getItems().get(getIndex());
                    fillForm(selectedProduct);
                });

                deleteBtn.getStyleClass().add("button");
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 6 10;");
                deleteBtn.setOnAction(e -> handleDeleteProduct(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadAdminData() {
        allProducts.setAll(productService.getAllProducts());
        productTable.setItems(allProducts);
    }

    private void setupAdminForm() {
        formCategoryCombo.setItems(FXCollections.observableArrayList("Supplements", "Skincare", "Haircare", "Hygiene", "Baby", "Wellness"));
    }

    private void fillForm(Parapharmacy product) {
        nameField.setText(product.getName());
        descArea.setText(product.getDescription());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        formCategoryCombo.setValue(product.getCategory());
        adminTitle.setText("Edit product");
        FormValidator.setMessage(feedbackLabel, "Editing " + product.getName(), false);
    }

    @FXML
    public void handleClearForm() {
        selectedProduct = null;
        nameField.clear();
        descArea.clear();
        priceField.clear();
        stockField.clear();
        formCategoryCombo.setValue(null);
        adminTitle.setText("Product management");
        FormValidator.setMessage(feedbackLabel, "", true);
        FormValidator.clearStates(nameField, priceField, stockField, formCategoryCombo);
    }

    @FXML
    public void handleSaveProduct() {
        FormValidator.clearStates(nameField, priceField, stockField, formCategoryCombo);

        String name = nameField.getText().trim();
        String desc = descArea.getText().trim();
        String category = formCategoryCombo.getValue();

        if (name.length() < 2) {
            FormValidator.markInvalid(nameField);
            FormValidator.setMessage(feedbackLabel, "Product name is required.", true);
            return;
        }
        if (desc.isEmpty()) {
            FormValidator.setMessage(feedbackLabel, "Description is required.", true);
            return;
        }
        if (category == null || category.isBlank()) {
            FormValidator.markInvalid(formCategoryCombo);
            FormValidator.setMessage(feedbackLabel, "Category is required.", true);
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            FormValidator.markInvalid(priceField);
            FormValidator.setMessage(feedbackLabel, "Price must be a valid number.", true);
            return;
        }

        try {
            stock = Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            FormValidator.markInvalid(stockField);
            FormValidator.setMessage(feedbackLabel, "Stock must be a whole number.", true);
            return;
        }

        if (price <= 0) {
            FormValidator.markInvalid(priceField);
            FormValidator.setMessage(feedbackLabel, "Price must be greater than 0.", true);
            return;
        }
        if (stock < 0) {
            FormValidator.markInvalid(stockField);
            FormValidator.setMessage(feedbackLabel, "Stock cannot be negative.", true);
            return;
        }

        Parapharmacy product = selectedProduct == null
                ? new Parapharmacy(name, desc, price, stock, category, "")
                : selectedProduct;

        product.setName(name);
        product.setDescription(desc);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);

        Integer excludedId = selectedProduct == null ? null : selectedProduct.getId();
        if (productService.productExists(product, excludedId)) {
            FormValidator.markInvalid(nameField);
            FormValidator.markInvalid(formCategoryCombo);
            FormValidator.setMessage(feedbackLabel, "A product with the same name and category already exists.", true);
            return;
        }

        boolean success = selectedProduct == null
                ? productService.addProduct(product)
                : productService.updateProduct(product);

        if (success) {
            boolean creating = selectedProduct == null;
            loadAdminData();
            handleClearForm();
            FormValidator.setMessage(feedbackLabel, creating ? "Product added successfully." : "Product updated successfully.", false);
        } else {
            FormValidator.setMessage(feedbackLabel, "The product could not be saved.", true);
        }
    }

    private void handleDeleteProduct(Parapharmacy product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + product.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES && productService.deleteProduct(product.getId())) {
                loadAdminData();
                if (selectedProduct != null && selectedProduct.getId() == product.getId()) {
                    handleClearForm();
                }
            }
        });
    }

    @FXML
    public void handleBackToShop() {
        loadSubView("/fxml/product_list.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ProductListController) {
                ((ProductListController) controller).setCurrentUser(currentUser);
            }

            StackPane mainContent = null;
            if (searchField != null && searchField.getScene() != null) {
                mainContent = (StackPane) searchField.getScene().lookup("#mainContent");
            } else if (adminTitle != null && adminTitle.getScene() != null) {
                mainContent = (StackPane) adminTitle.getScene().lookup("#mainContent");
            }

            if (mainContent != null) {
                mainContent.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
