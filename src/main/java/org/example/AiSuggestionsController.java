package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AiSuggestionsController {

    @FXML
    private Label notesLabel = new Label();

    @FXML
    private VBox recommendationsVBox = new VBox();

    @FXML
    private VBox riskVBox = new VBox();

    private final AiRecommendationService aiRecommendationService = new AiRecommendationService();
    private final RiskAnalysisService riskAnalysisService = new RiskAnalysisService();
    private final ServiceWishlist wishlistService = new ServiceWishlist();

    private String patientNotes = "";

    @FXML
    public void initialize() {
        renderRecommendations();
    }

    public void setPatientNotes(String patientNotes) {
        this.patientNotes = patientNotes == null ? "" : patientNotes;
        renderRecommendations();
    }

    public List<Product> getAiRecommendations(String patientNotes) {
        return aiRecommendationService.getAiRecommendations(patientNotes);
    }

    @FXML
    public void handleBackToAppointment() {
        NavigationManager.getInstance().showAppointments();
    }

    @FXML
    public void handleRefreshSuggestions() {
        renderRecommendations();
    }

    private void renderRecommendations() {
        if (recommendationsVBox == null) {
            return;
        }

        recommendationsVBox.getChildren().clear();
        if (riskVBox != null) {
            riskVBox.getChildren().clear();
            riskVBox.getChildren().add(createRiskCard(riskAnalysisService.analyze(patientNotes)));
        }
        notesLabel.setText(patientNotes == null || patientNotes.isBlank()
                ? "No notes provided yet."
                : "Patient notes: " + patientNotes);

        List<Product> recommendations = getAiRecommendations(patientNotes);
        if (recommendations.isEmpty()) {
            recommendationsVBox.getChildren().add(createMessage());
            return;
        }

        for (Product product : recommendations) {
            recommendationsVBox.getChildren().add(createRecommendationRow(product));
        }
    }

    private VBox createRiskCard(RiskAnalysisService.RiskAssessment riskAssessment) {
        VBox card = new VBox(6);
        card.getStyleClass().add("risk-analysis-card");

        Label titleLabel = new Label("Risk Assessment (" + riskAssessment.riskLevel() + ")");
        titleLabel.getStyleClass().add("risk-analysis-title");
        titleLabel.setWrapText(true);

        Label summaryLabel = new Label(riskAssessment.summary());
        summaryLabel.getStyleClass().add("risk-analysis-summary");
        summaryLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, summaryLabel);

        if (!riskAssessment.matchedKeywords().isEmpty()) {
            Label signalsLabel = new Label("Signals: " + String.join(", ", riskAssessment.matchedKeywords()));
            signalsLabel.getStyleClass().add("risk-analysis-signals");
            signalsLabel.setWrapText(true);
            card.getChildren().add(signalsLabel);
        }

        card.getStyleClass().add("risk-" + riskAssessment.riskLevel().toLowerCase());
        return card;
    }

    private HBox createRecommendationRow(Product product) {
        Label nameLabel = new Label(product.getName() + "  -  " + String.format("%.2f TND", product.getPrice()) + "  -  " + product.getCategory());
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("ai-recommendation-title");

        Button addToWishlistButton = new Button("Add to Wishlist");
        addToWishlistButton.getStyleClass().add("btn-primary");
        addToWishlistButton.setOnAction(event -> {
            try {
                boolean added = wishlistService.add(product);
                if (!added) {
                    showError("The selected product could not be added.");
                    return;
                }
                NavigationManager.getInstance().showWishlist();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Button goToParapharmacieButton = new Button("Go to Parapharmacie");
        goToParapharmacieButton.getStyleClass().add("btn-secondary");
        goToParapharmacieButton.setOnAction(event -> NavigationManager.getInstance().showParapharmacie());

        HBox row = new HBox(12, nameLabel, addToWishlistButton, goToParapharmacieButton);
        row.getStyleClass().add("ai-recommendation-row");
        return row;
    }

    private Label createMessage() {
        Label label = new Label("No recommendation available.");
        label.setStyle("-fx-text-fill: #666; -fx-padding: 8;");
        label.setWrapText(true);
        return label;
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wishlist Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

