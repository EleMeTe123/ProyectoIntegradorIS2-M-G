package com.is1.proyecto.controllers;

import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.UserService;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class ProfessorController {

    private final UserService userService;

    public ProfessorController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes() {
        get("/profesor/alta", this::showCreateForm, new MustacheTemplateEngine());
        post("/profesor/alta", this::handleCreateProfessor);
        get("/profile", this::showProfile, new MustacheTemplateEngine());
    }

    private ModelAndView showCreateForm(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);
        return new ModelAndView(model, "profesor_form.mustache");
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
            return redirectError(res, "/profesor/alta",
                    "Required fields are missing.");
        }

        int dni;
        try {
            dni = Integer.parseInt(dniStr.trim());
        } catch (NumberFormatException e) {
            return redirectError(res, "/profesor/alta", "The DNI number must be valid.");
        }

        if (userService.emailExists(email))
            return redirectError(res, "/profesor/alta", "Email already exists.");
        if (userService.dniExists(dni))
            return redirectError(res, "/profesor/alta", "DNI already exists.");
        if (userService.usernameExists(username))
            return redirectError(res, "/profesor/alta", "The username is already in use.");

        try {
            userService.createUser(new CreateUserRequest(
                    username, password, "PROFESSOR", firstName, lastName, email,
                    dni, address, phoneNumber));

            String msg = "Professor " + firstName + " " + lastName + " successfully registered. User: " + username;
            res.status(201);
            res.redirect("/profesor/alta?message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error registering the professor: " + e.getMessage());
            res.status(500);
            return redirectError(res, "/profesor/alta", "Internal error. Please try again.");
        }
        return "";
    }

    private ModelAndView showProfile(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();

        if (req.session().attribute("loggedIn") == null) {
            res.redirect("/login");
            return null;
        }

        String currentUserName = req.session().attribute("currentUserUsername");
        Integer userId = req.session().attribute("userId");

        model.put("userName", currentUserName);

        User user = userService.findById(userId);
        if (user != null && "PROFESSOR".equals(user.getString("rol"))) {
            model.put("isProfessor", true);
            model.put("firstName", user.getString("firstName"));
            model.put("lastName", user.getString("lastName"));
            model.put("dni", user.get("dni"));
            model.put("email", user.getString("email"));
            Object phoneNumber = user.get("phoneNumber");
            if (phoneNumber != null) model.put("phoneNumber", phoneNumber);
            String address = user.getString("address");
            if (address != null && !address.isEmpty()) model.put("address", address);
        } else {
            model.put("isProfessor", false);
        }

        return new ModelAndView(model, "profile.mustache");
    }

    private String redirectError(Response res, String route, String message) {
        res.status(400);
        res.redirect(route + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
        return "";
    }
}
