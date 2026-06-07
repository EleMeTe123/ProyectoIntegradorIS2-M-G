package com.is1.proyecto.controllers;

import com.is1.proyecto.dto.LoginRequest;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.AuthService;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void registerRoutes() {
        get("/", this::showLogin, new MustacheTemplateEngine());
        post("/login", this::handleLogin, new MustacheTemplateEngine());
        get("/logout", this::handleLogout);
    }

    private ModelAndView showLogin(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();
        String error = req.queryParams("error");
        String message = req.queryParams("message");
        if (error != null && !error.isEmpty()) model.put("errorMessage", error);
        if (message != null && !message.isEmpty()) model.put("successMessage", message);
        return new ModelAndView(model, "login.mustache");
    }

    private ModelAndView handleLogin(Request req, Response res) {
        Map<String, Object> model = new HashMap<>();

        LoginRequest loginReq = new LoginRequest(
                req.queryParams("username"),
                req.queryParams("password")
        );

        if (!loginReq.isValid()) {
            res.status(400);
            model.put("errorMessage", "Username and password are required.");
            return new ModelAndView(model, "login.mustache");
        }

        User user = authService.authenticate(loginReq);

        if (user == null) {
            res.status(401);
            model.put("errorMessage", "Incorrect username or password.");
            return new ModelAndView(model, "login.mustache");
        }

        req.session(true).attribute("currentUserUsername", user.getString("userName"));
        req.session().attribute("userId", user.getId());
        req.session().attribute("loggedIn", true);
        req.session().attribute("rol", user.getString("rol"));

        res.redirect("/dashboard");
        return null;
    }

    private String handleLogout(Request req, Response res) {
        req.session().invalidate();
        res.redirect("/");
        return null;
    }
}
