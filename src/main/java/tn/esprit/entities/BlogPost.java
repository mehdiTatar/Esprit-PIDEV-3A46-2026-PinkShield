package tn.esprit.entities;

import java.sql.Timestamp;

public class BlogPost {
    private int id;
    private String title;
    private String content;
    private String authorName;
    private String authorRole;
    private String imagePath;
    private Timestamp createdAt;

    public BlogPost() {}

    public BlogPost(int id, String title, String content, String authorName, String authorRole, String imagePath, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public BlogPost(String title, String content, String authorName, String authorRole, String imagePath) {
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "BlogPost{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}
