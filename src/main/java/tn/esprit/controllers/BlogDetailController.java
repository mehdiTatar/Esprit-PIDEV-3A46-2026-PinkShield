package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.BlogPost;
import tn.esprit.entities.Comment;
import tn.esprit.entities.User;
import tn.esprit.services.BlogService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BlogDetailController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentLabel;
    @FXML private Label commentCountLabel;
    @FXML private VBox commentsContainer;
    @FXML private TextArea commentArea;
    @FXML private HBox adminActions;

    private final BlogService blogService = new BlogService();
    private BlogPost currentPost;
    private User currentUser;

    public void setPost(BlogPost post) {
        this.currentPost = post;
        renderPost();
        loadComments();
        updateAdminActions();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateAdminActions();
    }

    private void renderPost() {
        titleLabel.setText(currentPost.getTitle());
        authorLabel.setText(currentPost.getAuthorName());
        roleLabel.setText(currentPost.getAuthorRole().toUpperCase());
        dateLabel.setText(currentPost.getCreatedAt().toLocalDateTime().format(DATE_FORMATTER));
        contentLabel.setText(currentPost.getContent());
    }

    private void updateAdminActions() {
        if (adminActions == null || currentUser == null || currentPost == null) {
            if (adminActions != null) {
                adminActions.setVisible(false);
                adminActions.setManaged(false);
            }
            return;
        }

        boolean canManage = "admin".equalsIgnoreCase(currentUser.getRole())
                || currentUser.getFullName().equalsIgnoreCase(currentPost.getAuthorName());
        adminActions.setVisible(canManage);
        adminActions.setManaged(canManage);
    }

    private void loadComments() {
        List<Comment> comments = blogService.getCommentsByPostId(currentPost.getId());
        commentCountLabel.setText("Comments (" + comments.size() + ")");
        commentsContainer.getChildren().clear();
        for (Comment comment : comments) {
            commentsContainer.getChildren().add(createCommentView(comment));
        }
    }

    private VBox createCommentView(Comment comment) {
        VBox view = new VBox(6);
        view.getStyleClass().add("panel-card");
        view.setStyle("-fx-padding: 16;");

        HBox header = new HBox(10);
        Label author = new Label(comment.getAuthorName());
        author.getStyleClass().add("auth-summary-title");

        Label date = new Label(comment.getCreatedAt().toLocalDateTime().format(DATE_FORMATTER));
        date.getStyleClass().add("table-meta");
        header.getChildren().addAll(author, date);

        Label content = new Label(comment.getContent());
        content.getStyleClass().add("dashboard-copy");
        content.setWrapText(true);

        view.getChildren().addAll(header, content);
        return view;
    }

    @FXML
    public void handleAddComment() {
        String text = commentArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Validation error", "Comment content is required.", Alert.AlertType.ERROR);
            return;
        }
        if (text.length() < 3) {
            showAlert("Validation error", "Comment must contain at least 3 characters.", Alert.AlertType.ERROR);
            return;
        }

        String author = currentUser != null ? currentUser.getFullName() : "Anonymous";
        Comment comment = new Comment(currentPost.getId(), author, text);
        if (blogService.addComment(comment)) {
            commentArea.clear();
            loadComments();
        } else {
            showAlert("Error", "Failed to add comment.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleBack() {
        loadSubView("/fxml/blog_list.fxml", false);
    }

    @FXML
    public void handleEdit() {
        loadSubView("/fxml/blog_form.fxml", true);
    }

    @FXML
    public void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this post?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES && blogService.deletePost(currentPost.getId())) {
                handleBack();
            }
        });
    }

    private void loadSubView(String fxmlPath, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BlogListController) {
                BlogListController blogController = (BlogListController) controller;
                blogController.setCurrentUser(currentUser);
                if (editMode) {
                    blogController.loadPostForEdit(currentPost);
                }
            }

            StackPane mainContent = (StackPane) titleLabel.getScene().lookup("#mainContent");
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
