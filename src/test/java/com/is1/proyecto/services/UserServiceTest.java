package com.is1.proyecto.services;

import com.is1.proyecto.config.DatabaseTestBase;
import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends DatabaseTestBase {

    private UserService userService;

    @BeforeEach
    void setUpService() {
        cleanTable("registrations");
        cleanTable("subjects");
        cleanTable("users");
        userService = new UserService();
    }

    @Test
    void createUserPersistsAndReturnsUser() {
        CreateUserRequest req = new CreateUserRequest("testuser", "secret", "STUDENT",
                "Test", "User", "test@test.com", 12345678, "Addr", "555-0000");
        User user = userService.createUser(req);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getString("userName"));
        assertEquals("STUDENT", user.getString("rol"));
    }

    @Test
    void createUserHashesPassword() {
        CreateUserRequest req = new CreateUserRequest("user1", "mypassword");
        User user = userService.createUser(req);
        String storedPw = user.getString("password");
        assertNotNull(storedPw);
        assertNotEquals("mypassword", storedPw);
    }

    @Test
    void findByUsernameReturnsCorrectUser() {
        userService.createUser(new CreateUserRequest("user_a", "pass"));
        User found = userService.findByUsername("user_a");
        assertNotNull(found);
        assertEquals("user_a", found.getString("userName"));
    }

    @Test
    void findByUsernameReturnsNullWhenNotFound() {
        assertNull(userService.findByUsername("nonexistent"));
    }

    @Test
    void findByIdReturnsCorrectUser() {
        User created = userService.createUser(new CreateUserRequest("findme", "pass"));
        int id = created.getInteger("id");
        User found = userService.findById(id);
        assertNotNull(found);
        assertEquals("findme", found.getString("userName"));
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertNull(userService.findById(9999));
    }

    @Test
    void usernameExistsReturnsTrueWhenExists() {
        userService.createUser(new CreateUserRequest("existing", "pass"));
        assertTrue(userService.usernameExists("existing"));
    }

    @Test
    void usernameExistsReturnsFalseWhenNotExists() {
        assertFalse(userService.usernameExists("ghost"));
    }

    @Test
    void emailExistsReturnsTrueWhenExists() {
        userService.createUser(new CreateUserRequest("user_email", "pass", "STUDENT",
                null, null, "unique@test.com", null, null, null));
        assertTrue(userService.emailExists("unique@test.com"));
    }

    @Test
    void emailExistsReturnsFalseWhenNotExists() {
        assertFalse(userService.emailExists("noone@test.com"));
    }

    @Test
    void dniExistsReturnsTrueWhenExists() {
        userService.createUser(new CreateUserRequest("user_dni", "pass", "STUDENT",
                null, null, null, 98765432, null, null));
        assertTrue(userService.dniExists(98765432));
    }

    @Test
    void dniExistsReturnsFalseWhenNotExists() {
        assertFalse(userService.dniExists(11111111));
    }

    @Test
    void createUserWithDuplicateUsernameThrowsException() {
        userService.createUser(new CreateUserRequest("duplicate", "pass"));
        assertThrows(Exception.class, () ->
                userService.createUser(new CreateUserRequest("duplicate", "otherpass")));
    }

    @Test
    void createUserStoresOptionalFields() {
        userService.createUser(new CreateUserRequest("fulluser", "pass", "PROFESSOR",
                "John", "Doe", "john@test.com", 55555555, "Some Street 123", "555-1234"));
        User user = userService.findByUsername("fulluser");
        assertEquals("John", user.getString("firstName"));
        assertEquals("Doe", user.getString("lastName"));
        assertEquals("john@test.com", user.getString("email"));
        assertEquals(55555555, user.getInteger("dni"));
        assertEquals("Some Street 123", user.getString("address"));
        assertEquals("555-1234", user.getString("phoneNumber"));
    }
}
