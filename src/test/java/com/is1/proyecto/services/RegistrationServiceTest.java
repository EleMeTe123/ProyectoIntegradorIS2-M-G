package com.is1.proyecto.services;

import com.is1.proyecto.config.DatabaseTestBase;
import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.Registration;
import com.is1.proyecto.models.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest extends DatabaseTestBase {

    private RegistrationService registrationService;
    private UserService userService;
    private SubjectService subjectService;
    private int studentId;
    private int professorId;
    private int subjectId;

    @BeforeEach
    void setUpService() {
        cleanTable("registrations");
        cleanTable("subjects");
        cleanTable("users");

        registrationService = new RegistrationService();
        userService = new UserService();
        subjectService = new SubjectService();

        professorId = userService.createUser(new CreateUserRequest("prof_1", "pass", "PROFESSOR",
                null, null, null, null, null, null)).getInteger("id");
        studentId = userService.createUser(new CreateUserRequest("student_1", "pass")).getInteger("id");
        subjectId = subjectService.createSubject("Algebra", "ALG101", professorId).getInteger("id");
    }

    @Test
    void enrollCreatesRegistration() {
        Registration reg = registrationService.enroll(studentId, subjectId);
        assertNotNull(reg);
        assertNotNull(reg.getInteger("id"));
        assertEquals(studentId, reg.getInteger("studentId"));
        assertEquals(subjectId, reg.getInteger("subjectId"));
    }

    @Test
    void isEnrolledReturnsTrueAfterEnrolling() {
        registrationService.enroll(studentId, subjectId);
        assertTrue(registrationService.isEnrolled(studentId, subjectId));
    }

    @Test
    void isEnrolledReturnsFalseBeforeEnrolling() {
        assertFalse(registrationService.isEnrolled(studentId, subjectId));
    }

    @Test
    void isEnrolledReturnsFalseForDifferentSubject() {
        int otherSubjectId = subjectService.createSubject("Fisica", "FIS101", professorId).getInteger("id");
        registrationService.enroll(studentId, subjectId);
        assertFalse(registrationService.isEnrolled(studentId, otherSubjectId));
    }

    @Test
    void doubleEnrollmentThrowsException() {
        registrationService.enroll(studentId, subjectId);
        assertThrows(Exception.class, () ->
                registrationService.enroll(studentId, subjectId));
    }

    @Test
    void findEnrolledSubjectsByStudentIdReturnsEmptyListWhenNotEnrolled() {
        List<Map<String, Object>> subjects = registrationService.findEnrolledSubjectsByStudentId(studentId);
        assertTrue(subjects.isEmpty());
    }

    @Test
    void findEnrolledSubjectsByStudentIdReturnsEnrolledSubjects() {
        registrationService.enroll(studentId, subjectId);
        List<Map<String, Object>> subjects = registrationService.findEnrolledSubjectsByStudentId(studentId);
        assertEquals(1, subjects.size());
        assertEquals("Algebra", subjects.get(0).get("name"));
        assertEquals("ALG101", subjects.get(0).get("code"));
    }

    @Test
    void findStudentsBySubjectIdReturnsEmptyListWhenNoStudents() {
        List<Map<String, Object>> students = registrationService.findStudentsBySubjectId(subjectId);
        assertTrue(students.isEmpty());
    }

    @Test
    void findStudentsBySubjectIdReturnsEnrolledStudents() {
        int anotherStudent = userService.createUser(new CreateUserRequest("student_2", "pass")).getInteger("id");
        registrationService.enroll(studentId, subjectId);
        registrationService.enroll(anotherStudent, subjectId);
        List<Map<String, Object>> students = registrationService.findStudentsBySubjectId(subjectId);
        assertEquals(2, students.size());
    }

    @Test
    void findStudentsBySubjectIdReturnsStudentDetails() {
        registrationService.enroll(studentId, subjectId);
        List<Map<String, Object>> students = registrationService.findStudentsBySubjectId(subjectId);
        assertEquals(1, students.size());
        assertEquals("student_1", students.get(0).get("userName"));
    }

    @Test
    void enrollmentUsesDifferentSubjectsAndStudents() {
        int studentB = userService.createUser(new CreateUserRequest("student_B", "pass")).getInteger("id");
        int subjectB = subjectService.createSubject("Geometria", "GEO101", professorId).getInteger("id");
        registrationService.enroll(studentId, subjectId);
        registrationService.enroll(studentB, subjectB);
        assertEquals(1, registrationService.findEnrolledSubjectsByStudentId(studentId).size());
        assertEquals(1, registrationService.findEnrolledSubjectsByStudentId(studentB).size());
        assertEquals(1, registrationService.findStudentsBySubjectId(subjectId).size());
        assertEquals(1, registrationService.findStudentsBySubjectId(subjectB).size());
    }
}
