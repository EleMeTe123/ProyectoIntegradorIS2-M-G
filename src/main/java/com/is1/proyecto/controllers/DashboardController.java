package com.is1.proyecto.controllers;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class DashboardController {

    public void registerRoutes() {
        get("/dashboard", this::showDashboard, new MustacheTemplateEngine());
    }

    private ModelAndView showDashboard(Request req, Response res) {
        String currentUsername = req.session().attribute("currentUserUsername");
        Boolean loggedIn = req.session().attribute("loggedIn");
        String rol = req.session().attribute("rol");

        if (currentUsername == null || loggedIn == null || !loggedIn) {
            res.redirect("/login?error=Login is required.");
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("username", currentUsername);

        if ("ADMIN".equals(rol)) {
            model.put("isAdmin", true);
        }

        return new ModelAndView(model, "dashboard.mustache");
    }
}
