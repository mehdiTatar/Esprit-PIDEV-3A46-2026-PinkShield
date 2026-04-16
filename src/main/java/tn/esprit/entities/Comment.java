package tn.esprit.entities;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int postId;
    private String authorName;
    private String content;
    private Timestamp createdAt;

    public Comment() {}

    public Comment(int id, int postId, String authorName, String content, Timestamp createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Comment(int postId, String authorName, String content) {
        this.postId = postId;
        this.authorName = authorName;
        this.content = content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}
