package com.is1.proyecto.services;

import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.User;

import java.util.List;

public class SubjectService {

    public Subject createSubject(String name, String code, int professorId) {
        Subject subject = new Subject();
        subject.set("name", name);
        subject.set("code", code);
        subject.set("professorId", professorId);
        subject.saveIt();
        return subject;
    }

    public boolean codeExists(String code) {
        return Subject.findFirst("code = ?", code) != null;
    }

    public List<User> findAllProfessors() {
        return User.where("rol = ?", "PROFESSOR");
    }

    public List<Subject> findByProfessorId(int professorId) {
        return Subject.where("professorId = ?", professorId);
    }
}
