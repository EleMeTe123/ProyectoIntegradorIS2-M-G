package com.is1.proyecto.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {

    @Test
    void minimalConstructorDefaultsToStudent() {
        CreateUserRequest req = new CreateUserRequest("juan", "pass123");
        assertEquals("juan", req.getUserName());
        assertEquals("pass123", req.getPassword());
        assertEquals("STUDENT", req.getRol());
        assertNull(req.getFirstName());
        assertNull(req.getEmail());
    }

    @Test
    void fullConstructorSetsAllFields() {
        CreateUserRequest req = new CreateUserRequest(
                "pedro", "pass", "PROFESSOR",
                "Pedro", "Gomez", "pedro@test.com",
                12345678, "Calle 123", "555-0100");
        assertEquals("pedro", req.getUserName());
        assertEquals("pass", req.getPassword());
        assertEquals("PROFESSOR", req.getRol());
        assertEquals("Pedro", req.getFirstName());
        assertEquals("Gomez", req.getLastName());
        assertEquals("pedro@test.com", req.getEmail());
        assertEquals(12345678, req.getDni());
        assertEquals("Calle 123", req.getAddress());
        assertEquals("555-0100", req.getPhoneNumber());
    }

    @Test
    void nullRolDefaultsToStudent() {
        CreateUserRequest req = new CreateUserRequest("a", "b", null, null, null, null, null, null, null);
        assertEquals("STUDENT", req.getRol());
    }

    @Test
    void isValidReturnsTrueWhenUsernameAndPasswordArePresent() {
        CreateUserRequest req = new CreateUserRequest("user", "pass");
        assertTrue(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsNull() {
        CreateUserRequest req = new CreateUserRequest(null, "pass");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenPasswordIsNull() {
        CreateUserRequest req = new CreateUserRequest("user", null);
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsEmpty() {
        CreateUserRequest req = new CreateUserRequest("", "pass");
        assertFalse(req.isValid());
    }

    @Test
    void isValidReturnsFalseWhenUsernameIsBlank() {
        CreateUserRequest req = new CreateUserRequest("   ", "pass");
        assertFalse(req.isValid());
    }
}
