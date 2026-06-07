package com.is1.proyecto.controllers;

import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes() {
        get("/user/create", this::showCreateForm, new MustacheTemplateEngine());
        get("/user/new", (req, res) -> new ModelAndView(new HashMap<>(), "user_form.mustache"), new MustacheTemplateEngine());
        post("/user/new", this::handleCreateUser);
        post("/add_users", this::handleAddUsersApi);
    }

    private ModelAndView showCreateForm(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);
        return new ModelAndView(model, "user_form.mustache");
    }

    private String handleCreateUser(Request req, Response res) {
        String userName = req.queryParams("userName");
        String password = req.queryParams("password");

        CreateUserRequest request = new CreateUserRequest(userName, password);

        if (!request.isValid()) {
            res.status(400);
            res.redirect("/user/create?error=Username and password required.");
            return "";
        }

        try {
            userService.createUser(request);
            res.status(201);
            res.redirect("/user/create?message=Account successfully created for " + userName + "!");
        } catch (Exception e) {
            System.err.println("Error registering account: " + e.getMessage());
            res.status(500);
            res.redirect("/user/create?error=Internal error creating the account. Please try again.");
        }
        return "";
    }

    private String handleAddUsersApi(Request req, Response res) {
        res.type("application/json");

        String userName = req.queryParams("userName");
        String password = req.queryParams("password");

        CreateUserRequest request = new CreateUserRequest(userName, password);

        if (!request.isValid()) {
            res.status(400);
            return "{\"error\": \"Username and password required.\"}";
        }

        try {
            var user = userService.createUser(request);
            res.status(201);
            return objectMapper.writeValueAsString(
                    Map.of("message", "User '" + userName + "' successfully registered.", "id", user.getId()));
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            res.status(500);
            return "{\"error\": \"Internal error registering user: " + e.getMessage() + "\"}";
        }
    }
}
