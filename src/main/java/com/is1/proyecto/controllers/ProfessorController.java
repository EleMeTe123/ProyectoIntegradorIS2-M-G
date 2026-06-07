package com.is1.proyecto.controllers;

import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.RegistrationService;
import com.is1.proyecto.services.SubjectService;
import com.is1.proyecto.services.UserService;
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

public class ProfessorController {

    private final UserService userService;
    private final SubjectService subjectService;
    private final RegistrationService registrationService;

    public ProfessorController(UserService userService, SubjectService subjectService, RegistrationService registrationService) {
        this.userService = userService;
        this.subjectService = subjectService;
        this.registrationService = registrationService;
    }

    public void registerRoutes() {
        get("/professor/create", this::showCreateForm, new MustacheTemplateEngine());
        post("/professor/create", this::handleCreateProfessor);
        get("/profile", this::showProfile, new MustacheTemplateEngine());
        get("/professor/subjects", this::showSubjects, new MustacheTemplateEngine());
        get("/professor/subject/:id/students", this::showSubjectStudents, new MustacheTemplateEngine());
    }

    private ModelAndView showCreateForm(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);
        return new ModelAndView(model, "professor_form.mustache");
    }

    private String handleCreateProfessor(Request req, Response res) {
        String username = req.queryParams("userName");
        String password = req.queryParams("password");
        String firstName = req.queryParams("firstName");
        String lastName = req.queryParams("lastName");
        String email = req.queryParams("email");
        String dniStr = req.queryParams("dni");
        String address = req.queryParams("address");
        String phoneNumber = req.queryParams("phoneNumber");

        if (username == null || username.isEmpty() || password == null || password.isEmpty() ||
            firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() ||
            email == null || email.isEmpty() || dniStr == null || dniStr.isEmpty() ) {
            return redirectError(res, "/professor/create",
                    "Required fields are missing.");
        }

        int dni;
        try {
            dni = Integer.parseInt(dniStr.trim());
        } catch (NumberFormatException e) {
            return redirectError(res, "/professor/create", "The DNI number must be valid.");
        }

        if (userService.emailExists(email))
            return redirectError(res, "/professor/create", "Email already exists.");
        if (userService.dniExists(dni))
            return redirectError(res, "/professor/create", "DNI already exists.");
        if (userService.usernameExists(username))
            return redirectError(res, "/professor/create", "The username is already in use.");

        try {
            userService.createUser(new CreateUserRequest(
                    username, password, "PROFESSOR", firstName, lastName, email,
                    dni, address, phoneNumber));

            String msg = "Professor " + firstName + " " + lastName + " successfully registered. User: " + username;
            res.status(201);
            res.redirect("/professor/create?message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error registering the professor: " + e.getMessage());
            res.status(500);
            return redirectError(res, "/professor/create", "Internal error. Please try again.");
        }
        return "";
    }

    private ModelAndView showProfile(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();

        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        Integer userId = req.session().attribute("userId");

        User user = userService.findById(userId);
        if (user != null) {
            model.put("userName", user.getString("userName"));
            model.put("rol", user.getString("rol"));
            model.put("firstName", user.getString("firstName"));
            model.put("lastName", user.getString("lastName"));
            model.put("dni", user.get("dni"));
            model.put("email", user.getString("email"));
            Object phoneNumber = user.get("phoneNumber");
            if (phoneNumber != null) model.put("phoneNumber", phoneNumber);
            String address = user.getString("address");
            if (address != null && !address.isEmpty()) model.put("address", address);
        }

        return new ModelAndView(model, "profile.mustache");
    }

    private ModelAndView showSubjects(Request req, Response res) {
        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        String rol = req.session().attribute("rol");
        if (!"PROFESSOR".equals(rol)) {
            res.redirect("/dashboard");
            return null;
        }

        Integer userId = req.session().attribute("userId");
        Map<String, Object> model = new HashMap<>();
        model.put("username", req.session().attribute("currentUserUsername"));

        if (userId != null) {
            List<Subject> subjects = subjectService.findByProfessorId(userId);
            List<Map<String, Object>> subjectList = new ArrayList<>();
            for (Subject s : subjects) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", s.getId());
                item.put("name", s.getString("name"));
                item.put("code", s.getString("code"));
                subjectList.add(item);
            }
            model.put("subjects", subjectList);
            model.put("hasSubjects", !subjectList.isEmpty());
        }

        return new ModelAndView(model, "professor_subjects.mustache");
    }

    private ModelAndView showSubjectStudents(Request req, Response res) {
        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        String rol = req.session().attribute("rol");
        if (!"PROFESSOR".equals(rol)) {
            res.redirect("/dashboard");
            return null;
        }

        String subjectIdStr = req.params(":id");
        int subjectId;
        try {
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException e) {
            res.redirect("/professor/subjects");
            return null;
        }

        Subject subject = subjectService.findById(subjectId);
        if (subject == null) {
            res.redirect("/professor/subjects");
            return null;
        }

        Integer professorId = req.session().attribute("userId");
        if (!subject.getInteger("professorId").equals(professorId)) {
            res.redirect("/professor/subjects");
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("subjectName", subject.getString("name"));
        model.put("subjectCode", subject.getString("code"));

        List<Map<String, Object>> students = registrationService.findStudentsBySubjectId(subjectId);
        model.put("students", students);
        model.put("hasStudents", !students.isEmpty());

        return new ModelAndView(model, "subject_students.mustache");
    }

    private String redirectError(Response res, String route, String message) {
        res.status(400);
        res.redirect(route + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
        return "";
    }
}
