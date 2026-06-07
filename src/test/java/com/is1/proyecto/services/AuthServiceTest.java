package com.is1.proyecto.services;

import com.is1.proyecto.config.DatabaseTestBase;
import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.dto.LoginRequest;
import com.is1.proyecto.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends DatabaseTestBase {

    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUpService() {
        cleanTable("registrations");
        cleanTable("subjects");
        cleanTable("users");
        authService = new AuthService();
        userService = new UserService();
    }

    @Test
    void authenticateReturnsUserWithValidCredentials() {
        userService.createUser(new CreateUserRequest("validuser", "correctpass"));
        LoginRequest login = new LoginRequest("validuser", "correctpass");
        User user = authService.authenticate(login);
        assertNotNull(user);
        assertEquals("validuser", user.getString("userName"));
    }

    @Test
    void authenticateReturnsNullWithWrongPassword() {
        userService.createUser(new CreateUserRequest("user1", "realpass"));
        LoginRequest login = new LoginRequest("user1", "wrongpass");
        assertNull(authService.authenticate(login));
    }

    @Test
    void authenticateReturnsNullWithNonExistentUser() {
        LoginRequest login = new LoginRequest("ghost", "somepass");
        assertNull(authService.authenticate(login));
    }

    @Test
    void isAdminReturnsTrueForAdminRole() {
        User admin = userService.createUser(new CreateUserRequest("admin1", "pass", "ADMIN",
                null, null, null, null, null, null));
        assertTrue(authService.isAdmin(admin));
    }

    @Test
    void isAdminReturnsFalseForStudentRole() {
        User student = userService.createUser(new CreateUserRequest("student1", "pass"));
        assertFalse(authService.isAdmin(student));
    }

    @Test
    void isAdminReturnsFalseForProfessorRole() {
        User prof = userService.createUser(new CreateUserRequest("prof1", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        assertFalse(authService.isAdmin(prof));
    }

    @Test
    void isProfessorReturnsTrueForProfessorRole() {
        User prof = userService.createUser(new CreateUserRequest("prof2", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        assertTrue(authService.isProfessor(prof));
    }

    @Test
    void isProfessorReturnsFalseForStudentRole() {
        User student = userService.createUser(new CreateUserRequest("student2", "pass"));
        assertFalse(authService.isProfessor(student));
    }

    @Test
    void isProfessorReturnsFalseForAdminRole() {
        User admin = userService.createUser(new CreateUserRequest("admin2", "pass", "ADMIN",
                null, null, null, null, null, null));
        assertFalse(authService.isProfessor(admin));
    }
}
