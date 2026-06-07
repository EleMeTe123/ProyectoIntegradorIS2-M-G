package com.is1.proyecto.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void constructorAndGetters() {
        LoginRequest req = new LoginRequest("admin", "secret");
        assertEquals("admin", req.getUsername());
        assertEquals("secret", req.getPassword());
    }

    @Test
    void isValidReturnsTrueWhenBothFieldsArePresent() {
        LoginRequest req = new LoginRequest("user", "pass");
        assertTrue(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsNull() {
        LoginRequest req = new LoginRequest(null, "pass");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenPasswordIsNull() {
        LoginRequest req = new LoginRequest("user", null);
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsEmpty() {
        LoginRequest req = new LoginRequest("", "pass");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsBlank() {
        LoginRequest req = new LoginRequest("   ", "pass");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenPasswordIsEmpty() {
        LoginRequest req = new LoginRequest("user", "");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenBothAreEmpty() {
        LoginRequest req = new LoginRequest("", "");
        assertFalse(req.isValid());
    }
}
