package tn.esprit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.entities.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testAddUser() {
        User newUser = new User();
        newUser.setFullName("Test User");
        newUser.setEmail("testuser" + System.currentTimeMillis() + "@test.com");
        newUser.setPassword("password123");
        newUser.setPhone("12345678");
        newUser.setAddress("Test address");
        newUser.setRole("user");

        boolean result = userService.addUser(newUser, "user");
        assertTrue(result, "User should be added successfully");
    }

    @Test
    public void testEmailExists() {
        boolean exists = userService.emailExists("admin@pinkshield.com");
        assertTrue(exists, "Email should exist in database");
    }

    @Test
    public void testEmailNotExists() {
        boolean exists = userService.emailExists("nonexistent@test.com");
        assertFalse(exists, "Email should not exist");
    }

    @Test
    public void testUpdateUser() {
        String seedEmail = "updateuser" + System.currentTimeMillis() + "@test.com";
        User user = new User();
        user.setFullName("Update User");
        user.setEmail(seedEmail);
        user.setPassword("password123");
        user.setPhone("12345678");
        user.setAddress("Test address");
        user.setRole("user");
        assertTrue(userService.addUser(user, "user"), "Seed user should be added");

        User createdUser = userService.getAllUsers().stream()
                .filter(existing -> seedEmail.equals(existing.getEmail()))
                .findFirst()
                .orElse(null);
        assertNotNull(createdUser, "Created user should exist");

        createdUser.setEmail("newemail" + System.currentTimeMillis() + "@test.com");
        boolean result = userService.updateUser(createdUser, "user");
        assertTrue(result, "User should be updated successfully");
    }

    @Test
    public void testDeleteUser() {
        String seedEmail = "deleteuser" + System.currentTimeMillis() + "@test.com";
        User user = new User();
        user.setFullName("Delete User");
        user.setEmail(seedEmail);
        user.setPassword("password123");
        user.setPhone("12345678");
        user.setAddress("Test address");
        user.setRole("user");
        assertTrue(userService.addUser(user, "user"), "Seed user should be added");

        User createdUser = userService.getAllUsers().stream()
                .filter(existing -> seedEmail.equals(existing.getEmail()))
                .findFirst()
                .orElse(null);
        assertNotNull(createdUser, "Created user should exist");

        boolean result = userService.deleteUser(createdUser.getId(), createdUser.getRole());
        assertTrue(result, "User should be deleted successfully");
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userService.getAllUsers();
        assertNotNull(users, "User list should not be null");
        assertTrue(users.size() > 0, "Should have at least one user");
    }

    @Test
    public void testSearchUsers() {
        List<User> results = userService.searchUsers("admin");
        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() >= 0, "Search should return results or empty list");
    }

    @Test
    public void testGetUserById() {
        User user = userService.getUserById(1, "admin");
        if (user != null) {
            assertNotNull(user.getEmail(), "User email should not be null");
        }
    }
}

