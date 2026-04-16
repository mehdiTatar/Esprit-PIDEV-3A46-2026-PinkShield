package tn.esprit.services;

import tn.esprit.entities.BlogPost;
import tn.esprit.entities.Comment;
import tn.esprit.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService {
    private Connection conn;

    public BlogService() {
        conn = MyDB.getInstance().getConnection();
    }

    // --- Blog Post CRUD ---

    public boolean addPost(BlogPost post) {
        String query = "INSERT INTO blog_post (title, content, author_name, author_role, image_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getAuthorName());
            ps.setString(4, post.getAuthorRole());
            ps.setString(5, post.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BlogPost> getAllPosts() {
        List<BlogPost> posts = new ArrayList<>();
        String query = "SELECT * FROM blog_post ORDER BY created_at DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                posts.add(new BlogPost(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("author_name"),
                        rs.getString("author_role"),
                        rs.getString("image_path"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean updatePost(BlogPost post) {
        String query = "UPDATE blog_post SET title = ?, content = ?, image_path = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImagePath());
            ps.setInt(4, post.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePost(int id) {
        String query = "DELETE FROM blog_post WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Comment CRUD ---

    public boolean addComment(Comment comment) {
        String query = "INSERT INTO comment (post_id, author_name, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, comment.getPostId());
            ps.setString(2, comment.getAuthorName());
            ps.setString(3, comment.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getInt("id"),
                            rs.getInt("post_id"),
                            rs.getString("author_name"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public boolean deleteComment(int id) {
        String query = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
