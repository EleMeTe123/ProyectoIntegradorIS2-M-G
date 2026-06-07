package com.is1.proyecto.services;

import com.is1.proyecto.models.Registration;
import org.javalite.activejdbc.Base;

import java.util.List;
import java.util.Map;

public class RegistrationService {

    public Registration enroll(int studentId, int subjectId) {
        Registration reg = new Registration();
        reg.set("studentId", studentId);
        reg.set("subjectId", subjectId);
        reg.saveIt();
        return reg;
    }

    public boolean isEnrolled(int studentId, int subjectId) {
        return Registration.findFirst("studentId = ? AND subjectId = ?", studentId, subjectId) != null;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findEnrolledSubjectsByStudentId(int studentId) {
        String sql = "SELECT s.id, s.name, s.code FROM subjects s " +
                     "INNER JOIN registrations r ON r.subjectId = s.id " +
                     "WHERE r.studentId = ?";
        List<?> result = Base.findAll(sql, studentId);
        return (List<Map<String, Object>>) result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findStudentsBySubjectId(int subjectId) {
        String sql = "SELECT u.id, u.userName, u.firstName, u.lastName, u.email, u.dni " +
                     "FROM users u INNER JOIN registrations r ON r.studentId = u.id " +
                     "WHERE r.subjectId = ?";
        List<?> result = Base.findAll(sql, subjectId);
        return (List<Map<String, Object>>) result;
    }
}
