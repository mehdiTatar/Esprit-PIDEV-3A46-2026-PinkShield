package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.BlogPost;
import tn.esprit.entities.User;
import tn.esprit.services.BlogService;
import tn.esprit.utils.FormValidator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BlogListController {
    @FXML private TextField searchField;
    @FXML private FlowPane postsContainer;
    @FXML private Button addPostBtn;

    @FXML private Label formTitle;
    @FXML private Label feedbackLabel;
    @FXML private TextField formTitleField;
    @FXML private TextArea formContentField;
    @FXML private TextField formImageField;
    @FXML private TextField authorRoleField;

    private final BlogService blogService = new BlogService();
    private List<BlogPost> allPosts;
    private User currentUser;
    private BlogPost editingPost;

    @FXML
    public void initialize() {
        if (postsContainer != null) {
            loadPosts();
        }

        if (authorRoleField != null) {
            FormValidator.attachClearOnInput(feedbackLabel, formTitleField, formImageField);
            formContentField.textProperty().addListener((obs, oldValue, newValue) -> FormValidator.setMessage(feedbackLabel, "", true));
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (authorRoleField != null) {
            authorRoleField.setText(user == null ? "USER" : user.getRole().toUpperCase());
        }
    }

    public void loadPostForEdit(BlogPost post) {
        this.editingPost = post;
        formTitle.setText("Edit Post");
        formTitleField.setText(post.getTitle());
        formContentField.setText(post.getContent());
        formImageField.setText(post.getImagePath() == null ? "" : post.getImagePath());
    }

    private void loadPosts() {
        allPosts = blogService.getAllPosts();
        renderPosts(allPosts);
    }

    private void renderPosts(List<BlogPost> posts) {
        postsContainer.getChildren().clear();
        for (BlogPost post : posts) {
            postsContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(BlogPost post) {
        VBox card = new VBox(15);
        card.getStyleClass().add("post-card");
        card.setPrefWidth(320);
        card.setPadding(new javafx.geometry.Insets(20));

        Label tag = new Label(post.getAuthorRole().toUpperCase());
        tag.getStyleClass().add("auth-status-pill");

        Label title = new Label(post.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        String text = post.getContent();
        if (text.length() > 120) {
            text = text.substring(0, 117) + "...";
        }
        Label excerpt = new Label(text);
        excerpt.getStyleClass().add("dashboard-copy");
        excerpt.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label author = new Label("By " + post.getAuthorName());
        author.getStyleClass().add("table-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button readMore = new Button("Read");
        readMore.getStyleClass().add("button");
        readMore.setStyle("-fx-font-size: 11; -fx-padding: 6 14;");
        readMore.setOnAction(event -> handleViewPost(post));

        footer.getChildren().addAll(author, spacer, readMore);
        card.getChildren().addAll(tag, title, excerpt, footer);
        return card;
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<BlogPost> filtered = allPosts.stream()
                .filter(post -> query.isEmpty()
                        || post.getTitle().toLowerCase().contains(query)
                        || post.getContent().toLowerCase().contains(query)
                        || post.getAuthorName().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderPosts(filtered);
    }

    @FXML
    public void handleNewPost() {
        loadSubView("/fxml/blog_form.fxml", null);
    }

    @FXML
    public void handleSavePost() {
        String title = formTitleField.getText().trim();
        String content = formContentField.getText().trim();
        String image = formImageField.getText().trim();

        if (title.length() < 5) {
            FormValidator.markInvalid(formTitleField);
            FormValidator.setMessage(feedbackLabel, "Post title must contain at least 5 characters.", true);
            return;
        }
        if (content.length() < 20) {
            FormValidator.setMessage(feedbackLabel, "Post content must contain at least 20 characters.", true);
            return;
        }

        String author = currentUser == null ? "Community User" : currentUser.getFullName();
        String role = currentUser == null ? "user" : currentUser.getRole();

        boolean success;
        if (editingPost == null) {
            BlogPost post = new BlogPost(title, content, author, role, image);
            success = blogService.addPost(post);
        } else {
            editingPost.setTitle(title);
            editingPost.setContent(content);
            editingPost.setImagePath(image);
            success = blogService.updatePost(editingPost);
        }

        if (success) {
            handleBackToList();
        } else {
            FormValidator.setMessage(feedbackLabel, "Unable to save the blog post.", true);
        }
    }

    @FXML
    public void handleCancelForm() {
        handleBackToList();
    }

    private void handleViewPost(BlogPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_detail.fxml"));
            Parent view = loader.load();

            BlogDetailController controller = loader.getController();
            controller.setPost(post);
            controller.setCurrentUser(currentUser);

            StackPane mainContent = (StackPane) postsContainer.getScene().lookup("#mainContent");
            if (mainContent != null) {
                mainContent.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBackToList() {
        loadSubView("/fxml/blog_list.fxml", null);
    }

    private void loadSubView(String fxmlPath, BlogPost postToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BlogListController) {
                BlogListController blogController = (BlogListController) controller;
                blogController.setCurrentUser(currentUser);
                if (postToEdit != null) {
                    blogController.loadPostForEdit(postToEdit);
                }
            }

            StackPane mainContent = null;
            if (postsContainer != null && postsContainer.getScene() != null) {
                mainContent = (StackPane) postsContainer.getScene().lookup("#mainContent");
            } else if (formTitle != null && formTitle.getScene() != null) {
                mainContent = (StackPane) formTitle.getScene().lookup("#mainContent");
            }

            if (mainContent != null) {
                mainContent.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openEditPost(BlogPost post) {
        loadSubView("/fxml/blog_form.fxml", post);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
