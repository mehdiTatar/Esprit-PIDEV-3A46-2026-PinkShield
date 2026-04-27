package org.example;

public final class UserSession {

    private static final UserSession INSTANCE = new UserSession();

    private int userId;
    private String email;
    private String name;
    private String role;

    private UserSession() {
        cleanUserSession();
    }

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public void setCurrentUser(int userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email == null ? "" : email.trim();
        this.name = name == null ? "" : name.trim();
        this.role = role == null ? "" : role.trim().toUpperCase();
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return userId > 0 && !email.isBlank();
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public void cleanUserSession() {
        this.userId = 0;
        this.email = "";
        this.name = "";
        this.role = "";
    }
}

