package com.is1.proyecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.controllers.AuthController;
import com.is1.proyecto.controllers.DashboardController;
import com.is1.proyecto.controllers.ProfessorController;
import com.is1.proyecto.controllers.StudentController;
import com.is1.proyecto.controllers.SubjectController;
import com.is1.proyecto.controllers.UserController;
import com.is1.proyecto.services.AuthService;
import com.is1.proyecto.services.RegistrationService;
import com.is1.proyecto.services.SubjectService;
import com.is1.proyecto.services.UserService;
import org.javalite.activejdbc.Base;

import static spark.Spark.*;

public class App {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        port(8080);

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        configureDatabaseFilters(dbConfig);
        registerControllers();
    }

    private static void configureDatabaseFilters(DBConfigSingleton dbConfig) {
        before((req, res) -> {
            try {
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}");
            }
        });

        after((req, res) -> {
            try {
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });
    }

    private static void registerControllers() {
        UserService userService = new UserService();
        AuthService authService = new AuthService();

        new AuthController(authService).registerRoutes();
        new UserController(userService, objectMapper).registerRoutes();
        new ProfessorController(userService, new SubjectService(), new RegistrationService()).registerRoutes();
        new SubjectController(new SubjectService()).registerRoutes();
        new StudentController(new RegistrationService()).registerRoutes();
        new DashboardController().registerRoutes();
    }
}
