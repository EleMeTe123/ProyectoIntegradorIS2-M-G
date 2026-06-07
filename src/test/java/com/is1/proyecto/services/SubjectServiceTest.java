package com.is1.proyecto.services;

import com.is1.proyecto.config.DatabaseTestBase;
import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubjectServiceTest extends DatabaseTestBase {

    private SubjectService subjectService;
    private UserService userService;

    @BeforeEach
    void setUpService() {
        cleanTable("registrations");
        cleanTable("subjects");
        cleanTable("users");
        subjectService = new SubjectService();
        userService = new UserService();
    }

    @Test
    void createSubjectPersistsAndReturnsSubject() {
        User prof = userService.createUser(new CreateUserRequest("prof", "pass", "PROFESSOR",
                "Profe", "Uno", "prof@test.com", 11111111, null, null));
        Subject subject = subjectService.createSubject("Matematica", "MAT101", prof.getInteger("id"));
        assertNotNull(subject);
        assertNotNull(subject.getInteger("id"));
        assertEquals("Matematica", subject.getString("name"));
        assertEquals("MAT101", subject.getString("code"));
        assertEquals(prof.getInteger("id"), subject.getInteger("professorId"));
    }

    @Test
    void codeExistsReturnsTrueWhenCodeExists() {
        User prof = userService.createUser(new CreateUserRequest("prof2", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        subjectService.createSubject("Fisica", "FIS101", prof.getInteger("id"));
        assertTrue(subjectService.codeExists("FIS101"));
    }

    @Test
    void codeExistsReturnsFalseWhenCodeNotExists() {
        assertFalse(subjectService.codeExists("NONEXISTENT"));
    }

    @Test
    void findAllProfessorsReturnsOnlyProfessors() {
        userService.createUser(new CreateUserRequest("student", "pass"));
        userService.createUser(new CreateUserRequest("prof_a", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        userService.createUser(new CreateUserRequest("admin", "pass", "ADMIN",
                null, null, null, null, null, null));
        userService.createUser(new CreateUserRequest("prof_b", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        List<User> professors = subjectService.findAllProfessors();
        assertEquals(2, professors.size());
        assertTrue(professors.stream().allMatch(u -> "PROFESSOR".equals(u.getString("rol"))));
    }

    @Test
    void findByProfessorIdReturnsCorrectSubjects() {
        User prof = userService.createUser(new CreateUserRequest("prof_x", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        User otherProf = userService.createUser(new CreateUserRequest("prof_y", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        subjectService.createSubject("Labo", "LAB101", prof.getInteger("id"));
        subjectService.createSubject("Labo II", "LAB102", prof.getInteger("id"));
        subjectService.createSubject("Otra", "OTR101", otherProf.getInteger("id"));
        List<Subject> subjects = subjectService.findByProfessorId(prof.getInteger("id"));
        assertEquals(2, subjects.size());
    }

    @Test
    void findByIdReturnsSubject() {
        User prof = userService.createUser(new CreateUserRequest("prof_z", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        Subject created = subjectService.createSubject("Quimica", "QUI101", prof.getInteger("id"));
        Subject found = subjectService.findById(created.getInteger("id"));
        assertNotNull(found);
        assertEquals("Quimica", found.getString("name"));
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertNull(subjectService.findById(9999));
    }

    @Test
    void findAllSubjectsReturnsAllSubjects() {
        User prof = userService.createUser(new CreateUserRequest("prof_all", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        subjectService.createSubject("S1", "C1", prof.getInteger("id"));
        subjectService.createSubject("S2", "C2", prof.getInteger("id"));
        List<Subject> all = subjectService.findAllSubjects();
        assertEquals(2, all.size());
    }

    @Test
    void createSubjectWithDuplicateCodeThrowsException() {
        User prof = userService.createUser(new CreateUserRequest("prof_dup", "pass", "PROFESSOR",
                null, null, null, null, null, null));
        subjectService.createSubject("Original", "CODE1", prof.getInteger("id"));
        assertThrows(Exception.class, () ->
                subjectService.createSubject("Duplicate", "CODE1", prof.getInteger("id")));
    }
}
