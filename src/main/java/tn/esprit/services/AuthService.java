package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    private final UserService userService = new UserService();

    public User authenticate(String email, String password) {
        Connection connection = MyDB.getInstance().getConnection();
        if (connection == null) {
            return null;
        }

        User user = checkTableForUser(connection, "admin", email, password, UserService.ROLE_ADMIN);
        if (user != null) {
            return user;
        }

        user = checkTableForUser(connection, "doctor", email, password, UserService.ROLE_DOCTOR);
        if (user != null) {
            return user;
        }

        return checkTableForUser(connection, "user", email, password, UserService.ROLE_USER);
    }

    public boolean emailExists(String email) {
        return userService.emailExists(email);
    }

    public boolean register(String fullName, String email, String password, String phone, String address) {
        User user = new User();
        user.setRole(UserService.ROLE_USER);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setAddress(address);

        String validationMessage = userService.validateUser(user, password, false);
        return validationMessage == null && userService.createUser(user);
    }

    public boolean registerPatient(String fullName, String email, String password, String phone, String address) {
        return register(fullName, email, password, phone, address);
    }

    public boolean registerDoctor(String firstName, String lastName, String email, String password, String speciality) {
        User user = new User();
        user.setRole(UserService.ROLE_DOCTOR);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setFullName((firstName + " " + lastName).trim());
        user.setEmail(email);
        user.setPassword(password);
        user.setSpeciality(speciality);

        String validationMessage = userService.validateUser(user, password, false);
        return validationMessage == null && userService.createUser(user);
    }

    private User checkTableForUser(Connection connection, String tableName, String email, String password, String role) {
        String query = "SELECT * FROM " + tableName + " WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password");
                if (!storedPassword.equals(password)) {
                    return null;
                }

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(storedPassword);
                user.setRole(role);

                if (UserService.ROLE_ADMIN.equals(role)) {
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setFullName((user.getFirstName() + " " + user.getLastName()).trim());
                } else if (UserService.ROLE_DOCTOR.equals(role)) {
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setFullName((user.getFirstName() + " " + user.getLastName()).trim());
                    user.setSpeciality(rs.getString("speciality"));
                } else {
                    user.setFullName(rs.getString("full_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                }

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            return null;
        }
    }
}
