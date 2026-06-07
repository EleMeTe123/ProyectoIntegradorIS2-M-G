package com.is1.proyecto.services;

import com.is1.proyecto.models.Registration;
import com.is1.proyecto.models.Subject;

import java.util.List;

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

    public List<Subject> findAllSubjects() {
        return Subject.findAll();
    }
}
