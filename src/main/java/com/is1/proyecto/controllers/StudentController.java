package com.is1.proyecto.controllers;

import com.is1.proyecto.models.Subject;
import com.is1.proyecto.services.RegistrationService;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class StudentController {

    private final RegistrationService registrationService;

    public StudentController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void registerRoutes() {
        get("/student/enroll", this::showEnrollForm, new MustacheTemplateEngine());
        post("/student/enroll", this::handleEnroll);
        get("/student/subjects", this::showMySubjects, new MustacheTemplateEngine());
    }

    private ModelAndView showEnrollForm(Request req, Response res) {
        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("username", req.session().attribute("currentUserUsername"));

        List<Subject> subjects = registrationService.findAllSubjects();
        Integer userId = req.session().attribute("userId");
        List<Map<String, Object>> subjectList = new ArrayList<>();
        for (Subject s : subjects) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("name", s.getString("name"));
            item.put("code", s.getString("code"));
            if (userId != null) {
                item.put("enrolled", registrationService.isEnrolled(userId, s.getInteger("id")));
            }
            subjectList.add(item);
        }
        model.put("subjects", subjectList);
        model.put("hasSubjects", !subjectList.isEmpty());

        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);

        return new ModelAndView(model, "student_enroll.mustache");
    }

    private String handleEnroll(Request req, Response res) {
        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return "";
        }

        Integer userId = req.session().attribute("userId");
        String subjectIdStr = req.queryParams("subjectId");

        if (userId == null || subjectIdStr == null || subjectIdStr.isEmpty()) {
            res.redirect("/student/enroll?error=Invalid request.");
            return "";
        }

        int subjectId;
        try {
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException e) {
            res.redirect("/student/enroll?error=Invalid subject.");
            return "";
        }

        if (registrationService.isEnrolled(userId, subjectId)) {
            res.redirect("/student/enroll?error=You are already enrolled in this subject.");
            return "";
        }

        try {
            registrationService.enroll(userId, subjectId);
            res.redirect("/student/enroll?message=Successfully enrolled in the subject.");
        } catch (Exception e) {
            System.err.println("Error enrolling: " + e.getMessage());
            res.redirect("/student/enroll?error=Internal error. Please try again.");
        }
        return "";
    }

    private ModelAndView showMySubjects(Request req, Response res) {
        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        Integer userId = req.session().attribute("userId");
        Map<String, Object> model = new HashMap<>();
        model.put("username", req.session().attribute("currentUserUsername"));

        if (userId != null) {
            List<Map<String, Object>> subjects = registrationService.findEnrolledSubjectsByStudentId(userId);
            model.put("subjects", subjects);
            model.put("hasSubjects", !subjects.isEmpty());
        }

        return new ModelAndView(model, "student_subjects.mustache");
    }
}
