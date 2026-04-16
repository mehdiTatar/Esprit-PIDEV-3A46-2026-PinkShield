package tn.esprit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.entities.User;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService();
    }

    @Test
    public void testAuthenticateAdmin() {
        User user = authService.authenticate("admin@pinkshield.com", "admin123");
        assertNotNull(user, "Admin user should authenticate successfully");
        assertEquals("admin@pinkshield.com", user.getEmail());
        assertEquals("admin", user.getRole());
    }

    @Test
    public void testAuthenticateDoctor() {
        User user = authService.authenticate("doctor@pinkshield.com", "doctor123");
        assertNotNull(user, "Doctor user should authenticate successfully");
        assertEquals("doctor@pinkshield.com", user.getEmail());
        assertEquals("doctor", user.getRole());
    }

    @Test
    public void testAuthenticateUser() {
        User user = authService.authenticate("patient@pinkshield.com", "user123");
        assertNotNull(user, "Regular user should authenticate successfully");
        assertEquals("patient@pinkshield.com", user.getEmail());
        assertEquals("user", user.getRole());
    }

    @Test
    public void testAuthenticateInvalidEmail() {
        User user = authService.authenticate("invalid@email.com", "password123");
        assertNull(user, "Authentication with invalid email should return null");
    }

    @Test
    public void testAuthenticateInvalidPassword() {
        User user = authService.authenticate("admin@pinkshield.com", "wrongpassword");
        assertNull(user, "Authentication with invalid password should return null");
    }

    @Test
    public void testEmailExists() {
        boolean exists = authService.emailExists("admin@pinkshield.com");
        assertTrue(exists, "Admin email should exist");
    }

    @Test
    public void testEmailNotExists() {
        boolean exists = authService.emailExists("nonexistent@test.com");
        assertFalse(exists, "Nonexistent email should return false");
    }

    @Test
    public void testRegisterNewUser() {
        String newEmail = "newuser" + System.currentTimeMillis() + "@test.com";
        boolean result = authService.register("New User", newEmail, "password123", "1234567890", "123 Test St");
        assertTrue(result, "New user registration should succeed");
        assertTrue(authService.emailExists(newEmail), "Newly registered email should exist");
    }

    @Test
    public void testRegisterDuplicateEmail() {
        boolean result = authService.register("Test User", "admin@pinkshield.com", "password123", "", "");
        assertFalse(result, "Registration with duplicate email should fail");
    }
}

