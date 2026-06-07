package com.is1.proyecto.controllers;

import com.is1.proyecto.models.User;
import com.is1.proyecto.services.SubjectService;
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

public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public void registerRoutes() {
        get("/subject/new", this::showCreateForm, new MustacheTemplateEngine());
        post("/subject/new", this::handleCreateSubject);
    }

    private ModelAndView showCreateForm(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        List<User> professors = subjectService.findAllProfessors();
        List<Map<String, Object>> professorList = new ArrayList<>();
        for (User p : professors) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            String firstName = p.getString("firstName");
            String lastName = p.getString("lastName");
            String userName = p.getString("userName");
            String displayName;
            if (firstName != null && lastName != null) {
                displayName = firstName + " " + lastName;
            } else {
                displayName = userName;
            }
            item.put("displayName", displayName);
            professorList.add(item);
        }
        model.put("professors", professorList);
        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);
        return new ModelAndView(model, "subject_form.mustache");
    }

    private String handleCreateSubject(Request req, Response res) {
        String name = req.queryParams("name");
        String code = req.queryParams("code");
        String professorIdStr = req.queryParams("professorId");

        if (name == null || name.isEmpty() || code == null || code.isEmpty() || professorIdStr == null || professorIdStr.isEmpty()) {
            res.status(400);
            res.redirect("/subject/new?error=All fields are required.");
            return "";
        }

        if (subjectService.codeExists(code)) {
            res.status(400);
            res.redirect("/subject/new?error=Code already exists.");
            return "";
        }

        int professorId;
        try {
            professorId = Integer.parseInt(professorIdStr);
        } catch (NumberFormatException e) {
            res.status(400);
            res.redirect("/subject/new?error=Invalid professor.");
            return "";
        }

        try {
            subjectService.createSubject(name, code, professorId);
            res.status(201);
            String msg = "Subject '" + name + "' successfully created.";
            res.redirect("/subject/new?message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error creating subject: " + e.getMessage());
            res.status(500);
            res.redirect("/subject/new?error=Internal error. Please try again.");
        }
        return "";
    }
}
