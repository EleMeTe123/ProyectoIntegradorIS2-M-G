package com.is1.proyecto; // Define el paquete de la aplicación, debe coincidir con la estructura de carpetas.

// Importaciones necesarias para la aplicación Spark
import com.fasterxml.jackson.databind.ObjectMapper; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import static spark.Spark.*; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).

// Importaciones específicas para ActiveJDBC (ORM para la base de datos)
import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import org.mindrot.jbcrypt.BCrypt; // Utilidad para hashear y verificar contraseñas de forma segura.

// Importaciones de Spark para renderizado de plantillas
import spark.ModelAndView; // Representa un modelo de datos y el nombre de la vista a renderizar.
import spark.template.mustache.MustacheTemplateEngine; // Motor de plantillas Mustache para Spark.

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
// Importaciones estándar de Java
import java.util.HashMap; // Para crear mapas de datos (modelos para las plantillas).
import java.util.List;
import java.util.Map; // Interfaz Map, utilizada para Map.of() o HashMap.

// Importaciones de clases del proyecto
import com.is1.proyecto.config.DBConfigSingleton; // Clase Singleton para la configuración de la base de datos.
import com.is1.proyecto.models.User; // Modelo de ActiveJDBC que representa la tabla 'users'.
import com.is1.proyecto.models.Profesores; // modelo para la tabla 'professors'
import com.is1.proyecto.models.Clase; //modelo  para la tabla clase
import com.is1.proyecto.models.Inscripcion; //modelo para los inscriptos por clase

//import librerias fechas
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    // Instancia estática y final de ObjectMapper para la
    // serialización/deserialización JSON.
    // Se inicializa una sola vez para ser reutilizada en toda la aplicación.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {
        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones
                    // (por defecto es 4567).

        // Obtener la instancia única del singleton de configuración de la base de
        // datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // --- Filtro 'before' para gestionar la conexión a la base de datos ---
        // Este filtro se ejecuta antes de cada solicitud HTTP.
        before((req, res) -> {
            try {
                // Le preguntamos a ActiveJDBC si YA hay una conexión abierta antes de intentar abrir otra
                if (!Base.hasConnection()) {
                    Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                }
                System.out.println(req.url());

            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
            }
        });

        // --- Filtro 'after' para cerrar la conexión a la base de datos ---
        // Este filtro se ejecuta después de que cada solicitud HTTP ha sido procesada.
        afterAfter((req, res) -> {
            try {
                // Solo cerramos si efectivamente hay una conexión que cerrar
                if (Base.hasConnection()) {
                    Base.close();
                }
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });
        
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            // Obtener y añadir mensaje de éxito de los query parameters (ej.
            // ?message=Cuenta creada!)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            // Obtener y añadir mensaje de error de los query parameters (ej. ?error=Campos
            // vacíos)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        get("/profesor/alta", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            // Obtengo y añado el mensaje de error de los query parameters
            // ( uno para exito, otro para error)
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            // aca crear la plantilla
            return new ModelAndView(model, "profesor_form.mustache");
        }, new MustacheTemplateEngine());

        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        // Requiere que el usuario esté autenticado.
        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantilla del dashboard.

            // Intenta obtener el nombre de usuario y la bandera de login de la sesión.
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador"); 


            // 1. Verificar si el usuario ha iniciado sesión.
            // Si no hay un nombre de usuario en la sesión, la bandera es nula o falsa,
            // significa que el usuario no está logueado o su sesión expiró.
            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                // Redirige al login con un mensaje de error.
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null; // Importante retornar null después de una redirección.
            }

            // 2. Si el usuario está logueado, añade el nombre de usuario al modelo para la
            // plantilla.
            model.put("username", currentUsername);

           if (esAdministrador != null && esAdministrador) {
                model.put("esAdministrador", true);
            }

            // New, Verificar si el usuario es profesor
            Integer userId = req.session().attribute("userId");
            if (userId != null) {
                Profesores profe = Profesores.findById(userId);
                if (profe != null) {
                    model.put("isProfessor", true);
                }
            }            
            
            // 3. Renderiza la plantilla del dashboard con el nombre de usuario.
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            // Invalida completamente la sesión del usuario.
            // Esto elimina todos los atributos guardados en la sesión y la marca como
            // inválida.
            // La cookie JSESSIONID en el navegador también será gestionada para
            // invalidarse.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");

            // Redirige al usuario a la página de login con un mensaje de éxito.
            res.redirect("/");

            return null; // Importante retornar null después de una redirección.
        });

        // GET: Muestra el formulario de inicio de sesión (login).
        // Nota: Esta ruta debería ser capaz de leer también mensajes de error/éxito de
        // los query params
        // si se la usa como destino de redirecciones. (Tu código de /user/create ya lo
        // hace, aplicar similar).
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta de alias para el formulario de creación de cuenta.
        // En una aplicación real, probablemente querrías unificar con '/user/create'
        // para evitar duplicidad.
        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache"); // No pasa un modelo específico, solo el
                                                                            // formulario.
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ver perfil del usuario
        get("/profile", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Verificación de sesión
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return null;
            }

            // Obtener datos básicos del usuario
            String name = req.session().attribute("currentUserUsername");
            Integer userId = req.session().attribute("userId");
            
            System.out.println("DEBUG: ID de usuario en sesión: " + userId);
            
            model.put("name", name);

            // Buscar si este usuario es un profesor
            // Como comparten ID (id_prof = user_id), buscamos por ID directamente.
            Profesores profe = Profesores.findById(userId);
            System.out.println("DEBUG: Resultado de búsqueda de profesor: " + profe);

            if (profe != null) {
                // Es un profesor: Pasamos sus datos a la vista
                model.put("isProfessor", true);
                model.put("nombre", profe.getString("nombre"));
                model.put("apellido", profe.getString("apellido"));
                model.put("dni", profe.get("dni"));
                model.put("legajo", profe.get("legajo"));
                model.put("cargo", profe.getString("cargo"));
                model.put("correo", profe.getString("correo"));
                
                // Manejo de campos opcionales para que no muestre "null"
                Object tel = profe.get("telefono");
                if (tel != null) model.put("telefono", tel);
                
                String dir = profe.getString("direccion");
                if (dir != null && !dir.isEmpty()) model.put("direccion", dir);
                
            } else {
                // No es profesor (es solo admin o usuario base)
                model.put("isProfessor", false);
            }

            return new ModelAndView(model, "profile.mustache");
        }, new MustacheTemplateEngine());

        // GET: Mostrar formulario para cambiar contraseña (Cualquier usuario logueado)
        get("/profile/password", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Verificación de seguridad: ¿Está logueado?
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return null;
            }

            // 2. Manejar mensajes de éxito o error que le enviaremos desde el POST
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // 3. Renderizar la vista
            return new ModelAndView(model, "cambiar_password.mustache");
        }, new MustacheTemplateEngine());

        // GET: página de configuraciones
        get("/settings", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Verificación de login
            if (req.session().attribute("loggedIn") == null) {
            res.redirect("/");
            return null;
            }

            // mando el nombre de usuario para la pagina
            String name = req.session().attribute("currentUserUsername");
            model.put("username", name);

            //fecha y hora actual
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
            
            model.put("fechaActual", ahora.format(formatoFecha));
            model.put("horaActual", ahora.format(formatoHora));

            // creacion de plantilla mustache para settings
            return new ModelAndView(model, "settings.mustache");
        }, new MustacheTemplateEngine());

        // GET: Mostrar lista de profesores para borrar (Solo Admin)
        get("/profesor/borrar", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Verificación de seguridad: ¿Está logueado y es administrador?
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador");
            
            if (loggedIn == null || !loggedIn || esAdministrador == null || !esAdministrador) {
                // Si no es admin, lo pateamos de vuelta al dashboard
                res.redirect("/dashboard");
                return null;
            }

            // 2. Buscar a todos los profesores en la base de datos
            // Profesores.findAll() trae todos los registros de la tabla 'professors'
            List<Profesores> listaProfesores = Profesores.findAll();
            model.put("profesores", listaProfesores);

            // 3. Manejar mensajes de éxito o error (por si venimos de borrar uno)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // 4. Renderizar la vista (que crearemos en la Fase 2)
            return new ModelAndView(model, "borrar_profesor.mustache");
        }, new MustacheTemplateEngine());

        // --- Rutas POST para manejar envíos de formularios y APIs ---

        // POST: Maneja el envío del formulario de creación de nueva cuenta.
        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // Validaciones básicas: campos no pueden ser nulos o vacíos.
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formulario de creación con un mensaje de error.
                res.redirect("/user/create?error=Nombre y contraseña son requeridos.");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }

            try {
                // Intenta crear y guardar la nueva cuenta en la base de datos.
                User ac = new User(); // Crea una nueva instancia del modelo User.
                // Hashea la contraseña de forma segura antes de guardarla.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                ac.set("name", name); // Asigna el nombre de usuario.
                ac.set("password", hashedPassword); // Asigna la contraseña hasheada.
                ac.set("esAdministrador", 0); //El usuario NO es administrador
                ac.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                // Redirige al formulario de creación con un mensaje de éxito.
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
                return ""; // Retorna una cadena vacía.

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB (ej. nombre de usuario
                // duplicado),
                // se captura aquí y se redirige con un mensaje de error.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return ""; // Retorna una cadena vacía.
            }
        });

        // POST: Maneja el envío del formulario de inicio de sesión.
        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantilla de login o dashboard.

            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            // Validaciones básicas: campos de usuario y contraseña no pueden ser nulos o
            // vacíos.
            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400); // Bad Request.
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }

            // Busca la cuenta en la base de datos por el nombre de usuario.
            User ac = User.findFirst("name = ?", username);

            // Si no se encuentra ninguna cuenta con ese nombre de usuario.
            if (ac == null) {
                res.status(401); // Unauthorized.
                model.put("errorMessage", "Usuario o contraseña incorrectos."); // Mensaje genérico por seguridad.
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }

            // Obtiene la contraseña hasheada almacenada en la base de datos.
            String storedHashedPassword = ac.getString("password");

            // Compara la contraseña en texto plano ingresada con la contraseña hasheada
            // almacenada.
            // BCrypt.checkpw hashea la plainTextPassword con el salt de
            // storedHashedPassword y compara.
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                // Autenticación exitosa.
                res.status(200); // OK.

                // --- Gestión de Sesión ---
                req.session(true).attribute("currentUserUsername", username); // Guarda el nombre de usuario en la
                                                                              // sesión.
                req.session().attribute("userId", ac.getId()); // Guarda el ID de la cuenta en la sesión (útil).
                req.session().attribute("loggedIn", true); // Establece una bandera para indicar que el usuario está
                                                           // logueado.

                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
                System.out.println("DEBUG: ID de Sesión: " + req.session().id());

                
                //Aseguramos la conversión a Boolean
                // Leemos el valor como un Object y verificamos si es Integer o Number.
                Object adminValue = ac.get("esAdministrador");
                boolean isAdmin = false;
                
                if (adminValue != null) {
                    // Si el valor es un Integer (o Number), lo comparamos con 1.
                    if (adminValue instanceof Number) {
                        isAdmin = ((Number) adminValue).intValue() == 1;
                    }
                    // Si el valor es directamente un Boolean, lo usamos.
                    else if (adminValue instanceof Boolean) {
                        isAdmin = (Boolean) adminValue;
                    }
                }
                
                req.session().attribute("esAdministrador", isAdmin);
                
                System.out.println("DEBUG LOGIN: El usuario '" + username + "' tiene esAdministrador=" + adminValue + ". El flag en sesión es: " + isAdmin);

                // REDIRECCIÓN INMEDIATA al GET /dashboard para que cargue la página
                res.redirect("/dashboard");
                return null;
                
                
            } else {
                // Contraseña incorrecta.
                res.status(401); // Unauthorized.
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos."); // Mensaje genérico por seguridad.
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta POST.

        // POST: Maneja el envío del formulario de Alta de Profesor (HU001)
        post("/profesor/alta", (req, res) -> {
            // datos del profesor
            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String correo = req.queryParams("correo");
            String dniStr = req.queryParams("dni");
            String direccion = req.queryParams("direccion");
            String telefonoStr = req.queryParams("telefono");
            String legajoStr = req.queryParams("legajo");
            String cargo = req.queryParams("cargo");
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // Validaciones básicas: campos no pueden ser nulos o vacíos.
            // ... (dentro de post /profesor/alta) ...

            // Validaciones básicas
            if (nombre == null || nombre.isEmpty() ||
                    apellido == null || apellido.isEmpty() ||
                    correo == null || correo.isEmpty() ||
                    dniStr == null || dniStr.isEmpty() ||
                    legajoStr == null || legajoStr.isEmpty() ||
                    password == null || password.isEmpty() ||
                    name == null || name.isEmpty()) { 

                res.status(400);
                String msg = "Faltan campos obligatorios: nombre, apellido, correo, DNI, legajo, usuario y contraseña son requeridos.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }

            // Validar formato de correo
            if (!correo.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                res.status(400);
                String msg = "El formato del correo electrónico no es válido.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }

            // Validar números
            Integer dni, legajo;
            try {
                dni = Integer.valueOf(dniStr.trim());
                legajo = Integer.valueOf(legajoStr.trim());
            } catch (NumberFormatException e) {
                res.status(400);
                String msg = "DNI y Legajo deben ser números válidos.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }

            // Validar duplicados (Correo)
            if (Profesores.findFirst("correo = ?", correo) != null) {
                res.status(409);
                String msg = "El correo electrónico ya existe en la base de datos.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
            // Validar duplicados (DNI)
            if (Profesores.findFirst("dni = ?", dni) != null) {
                res.status(409);
                String msg = "El DNI ya existe en la base de datos.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
            // Validar duplicados (Legajo)
            if (Profesores.findFirst("legajo = ?", legajo) != null) {
                res.status(409);
                String msg = "El número de legajo ya existe en la base de datos.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
            // Validar duplicados (Username)
            if (User.findFirst("name = ?", name) != null) {
                res.status(409);
                String msg = "El nombre de usuario ya está en uso. Elija otro.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
            
            try {
                // crear un nuevo usuario
                User newUser = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                newUser.set("name", name);
                newUser.set("password", hashedPassword);
                newUser.saveIt();
                
                Object userId = newUser.getId(); 
                System.out.println("DEBUG: Usuario creado con ID: " + userId);

                String sql = "INSERT INTO professors (id_prof, nombre, apellido, dni, legajo, correo, cargo, direccion, telefono) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                // Preparamos los valores opcionales para que sean null si están vacíos
                Object dirVal = (direccion != null && !direccion.isEmpty()) ? direccion : null;
                Object telVal = null;
                if (telefonoStr != null && !telefonoStr.isEmpty()) {
                     try { telVal = Integer.valueOf(telefonoStr.trim()); } catch (Exception e) {}
                }

                // Ejecutamos la inserción manual vinculando los IDs
                Base.exec(sql, userId, nombre, apellido, dni, legajo, correo, cargo, dirVal, telVal);

                System.out.println("DEBUG: Profesor insertado manualmente con ID: " + userId);

                // si el profesor se creo tenemos un user con exito
                res.status(201);
                String msg1 = "Profesor " + nombre + " " + apellido + " registrado con éxito. Su usuario inicial es: " + name;
                res.redirect("/profesor/alta?message=" + URLEncoder.encode(msg1, StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar profesor: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                String msg = "Error interno al registrar profesor. Intente nuevamente.";
                res.redirect("/profesor/alta?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
        });

        // POST: Maneja la eliminación de un profesor (Solo Admin)
        post("/profesor/borrar", (req, res) -> {
            
            // 1. Verificación de seguridad: ¿Es administrador?
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador");
            
            if (loggedIn == null || !loggedIn || esAdministrador == null || !esAdministrador) {
                res.redirect("/dashboard");
                return "";
            }

            // 2. Obtener el ID del profesor que el usuario seleccionó en la lista desplegable
            String profesorIdStr = req.queryParams("profesor_id");
            
            if (profesorIdStr != null && !profesorIdStr.isEmpty()) {
                try {
                    int profesorId = Integer.parseInt(profesorIdStr);
                    
                    // 3. Buscar al profesor en la base de datos
                    Profesores profe = Profesores.findById(profesorId);
                    
                    if (profe != null) {
                        // Guardamos el nombre para mostrarlo en el mensaje de éxito
                        String nombreCompleto = profe.getString("nombre") + " " + profe.getString("apellido");
                        
                        // 4. Eliminar de la tabla 'professors'
                        profe.delete(); 
                        
                        // 5. Eliminar de la tabla 'users' (Opcional, pero recomendado para mantener limpieza)
                        User usuarioAsociado = User.findById(profesorId);
                        if (usuarioAsociado != null) {
                            usuarioAsociado.delete();
                        }
                        
                        // 6. Redirigir de vuelta a la página de borrar con un mensaje verde de éxito
                        String exitoMsg = "El profesor " + nombreCompleto + " fue eliminado correctamente.";
                        res.redirect("/profesor/borrar?message=" + URLEncoder.encode(exitoMsg, StandardCharsets.UTF_8));
                        return "";
                    }
                } catch (Exception e) {
                    System.err.println("Error al intentar borrar el profesor: " + e.getMessage());
                    String errorMsg = "Error interno de la base de datos al intentar eliminar.";
                    res.redirect("/profesor/borrar?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                    return "";
                }
            }
            
            // Si el ID llega nulo o vacío (el usuario no seleccionó nada)
            String errorMsg = "Por favor, seleccione un profesor válido de la lista.";
            res.redirect("/profesor/borrar?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            return "";
            
        });

        // GET: Mostrar Opciones Avanzadas (Lista de todos los usuarios)
        get("/admin/usuarios", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Verificación de seguridad súper estricta: ¿Está logueado y es ADMIN?
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador");
            
            if (loggedIn == null || !loggedIn || esAdministrador == null || !esAdministrador) {
                res.redirect("/dashboard");
                return null;
            }

            // 2. Buscar TODOS los usuarios en la base de datos
            List<User> todosLosUsuarios = User.findAll();
            
            // Creamos una lista especial para mandarle a la vista (Mustache)
            // Usamos java.util.ArrayList para no tener que agregar imports extras arriba
            List<Map<String, Object>> listaParaVista = new java.util.ArrayList<>();

            for (User u : todosLosUsuarios) {
                Map<String, Object> datosUsuario = new HashMap<>();
                
                datosUsuario.put("id", u.getId());
                datosUsuario.put("name", u.getString("name"));
                
                // 3. Verificamos si es Administrador (Manejando si es Integer o Boolean en BD)
                Object adminValue = u.get("esAdministrador");
                boolean isAdmin = false;
                if (adminValue instanceof Number) {
                    isAdmin = ((Number) adminValue).intValue() == 1;
                } else if (adminValue instanceof Boolean) {
                    isAdmin = (Boolean) adminValue;
                }
                datosUsuario.put("isAdmin", isAdmin);

                // 4. Verificamos el Tipo de Cuenta (¿Existe en la tabla profesores?)
                Profesores profe = Profesores.findById(u.getId());
                if (profe != null) {
                    datosUsuario.put("tipoCuenta", "Profesor");
                } else {
                    datosUsuario.put("tipoCuenta", "Usuario Común");
                }

                // Agregamos este usuario procesado a nuestra lista
                listaParaVista.add(datosUsuario);
            }

            model.put("usuarios", listaParaVista);

            // 5. Manejar mensajes de éxito o error (para cuando borremos después)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // 6. Renderizar la vista (que crearemos en la Fase 2)
            return new ModelAndView(model, "opciones_avanzadas.mustache");
        }, new MustacheTemplateEngine());

        // GET: Menú intermedio para Administrar Clases (Solo Profesores)
        get("/profesor/clases", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Verificación de seguridad: ¿Está logueado?
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return null;
            }

            // 2. Verificación estricta: ¿Es realmente un profesor?
            Integer userId = req.session().attribute("userId");
            Profesores profe = Profesores.findById(userId);
            
            if (profe == null) {
                // Si intenta entrar un alumno o un admin que no es profesor, lo devolvemos
                res.redirect("/dashboard");
                return null;
            }

            // 3. Renderizar el menú de clases
            return new ModelAndView(model, "administrar_clases.mustache");
        }, new MustacheTemplateEngine());

        // GET: Mostrar formulario para crear una nueva clase
        get("/profesor/clases/nueva", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Verificación de seguridad
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return null;
            }
            Integer userId = req.session().attribute("userId");
            if (Profesores.findById(userId) == null) {
                res.redirect("/dashboard");
                return null;
            }

            // 2. Manejar mensajes
            String successMessage = req.queryParams("message");
            if (successMessage != null) model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null) model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "crear_clase.mustache");
        }, new MustacheTemplateEngine());

        // GET: Mostrar lista de clases del profesor (VERSIÓN DEBUG SOLUCIONADA)
        get("/profesor/clases/lista", (req, res) -> {
            try {
                Map<String, Object> model = new HashMap<>();
                
                // 1. Verificación de sesión
                if (req.session().attribute("loggedIn") == null) {
                    res.redirect("/login"); 
                    return null;
                }
                
                // 2. Conversión segura del ID
                Object userIdObj = req.session().attribute("userId");
                Integer userId = Integer.valueOf(userIdObj.toString());
                
                if (Profesores.findById(userId) == null) {
                    res.redirect("/dashboard"); 
                    return null;
                }

                // 3. Buscar en la base de datos
                List<Clase> misClases = Clase.where("profesor_id = ?", userId);
                List<Map<String, Object>> clasesParaVista = new java.util.ArrayList<>();
                
                for (Clase c : misClases) {
                    Map<String, Object> datosClase = new HashMap<>();
                    datosClase.put("idClase", c.getIdClase());
                    datosClase.put("nombre", c.getNombre());
                    clasesParaVista.add(datosClase);
                }
                
                model.put("clases", clasesParaVista);
                
                return new ModelAndView(model, "lista_clases.mustache");

            } catch (Exception e) {
                System.err.println("ERROR CRÍTICO EN LISTA DE CLASES:");
                e.printStackTrace();
                
                // SOLUCIÓN AL ERROR DE COMPILACIÓN: Usamos halt() para imprimir el error HTML 
                // y detener la ruta sin romper la regla del ModelAndView.
                String errorHtml = "<div style='font-family: sans-serif; padding: 20px; color: red;'>" +
                                   "<h2>¡Te atrapé, Error!</h2>" +
                                   "<p><strong>Mensaje:</strong> " + e.toString() + "</p>" +
                                   "<p><strong>Causa exacta:</strong> " + e.getCause() + "</p>" +
                                   "</div>";
                halt(500, errorHtml);
                return null;
            }
        }, new MustacheTemplateEngine());

        // GET: Ver detalles de una clase específica (Con gestión de alumnos)
        get("/profesor/clases/detalle", (req, res) -> {
         Map<String, Object> model = new HashMap<>();

         // Verificación de seguridad
         if (req.session().attribute("loggedIn") == null) {
             res.redirect("/login"); return null;
         }
         Object userIdObj = req.session().attribute("userId");
         Integer userId = Integer.valueOf(userIdObj.toString());

         if (Profesores.findById(userId) == null) {
             res.redirect("/dashboard"); return null;
         }

         String claseIdStr = req.queryParams("clase_id");

         if (claseIdStr != null && !claseIdStr.isEmpty()) {
             try {
                 int idClase = Integer.parseInt(claseIdStr);
                 Clase clase = Clase.findById(idClase);

                 if (clase != null && clase.getProfesorId().equals(userId)) {
                     model.put("claseId", clase.getIdClase());
                     model.put("claseNombre", clase.getNombre());
                     model.put("claseDescripcion", clase.getDescripcion());

                     // --- MAGIA NUEVA: OBTENER ALUMNOS INSCRITOS ---
                     List<Inscripcion> inscripciones = Inscripcion.where("clase_id = ?", idClase);
                     List<Map<String, Object>> alumnosInscritos = new java.util.ArrayList<>();
                     List<Integer> idsInscritos = new java.util.ArrayList<>();

                     for (Inscripcion ins : inscripciones) {
                         User u = User.findById(ins.getUserId());
                         if (u != null) {
                             Map<String, Object> map = new HashMap<>();
                             map.put("inscripcionId", ins.getId());
                             map.put("usuarioId", u.getId());
                             map.put("nombre", u.getString("name"));
                             alumnosInscritos.add(map);
                             idsInscritos.add(Integer.valueOf(u.getId().toString()));
                         }
                     }
                     model.put("alumnosInscritos", alumnosInscritos);

                     // --- MAGIA NUEVA: OBTENER ALUMNOS DISPONIBLES ---
                     // (Solo Usuarios Comunes que NO estén ya en la clase)
                     List<User> todosUsuarios = User.findAll();
                     List<Map<String, Object>> alumnosDisponibles = new java.util.ArrayList<>();

                     for (User u : todosUsuarios) {
                         Integer uid = Integer.valueOf(u.getId().toString());

                         Object adminVal = u.get("esAdministrador");
                         boolean isAdmin = false;
                         if (adminVal instanceof Number) isAdmin = ((Number) adminVal).intValue() == 1;
                         else if (adminVal instanceof Boolean) isAdmin = (Boolean) adminVal;

                         boolean isProf = Profesores.findById(uid) != null;

                         if (!isAdmin && !isProf && !idsInscritos.contains(uid)) {
                             Map<String, Object> map = new HashMap<>();
                             map.put("usuarioId", uid);
                             map.put("nombre", u.getString("name"));
                             alumnosDisponibles.add(map);
                         }
                     }
                     model.put("alumnosDisponibles", alumnosDisponibles);

                     // Mensajes de éxito y error
                     String successMessage = req.queryParams("message");
                     if (successMessage != null) model.put("successMessage", successMessage);
                     String errorMessage = req.queryParams("error");
                     if (errorMessage != null) model.put("errorMessage", errorMessage);

                     return new ModelAndView(model, "detalle_clase.mustache");
                 }
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
         }
         res.redirect("/profesor/clases/lista?error=Clase no encontrada.");
         return null;
        }, new MustacheTemplateEngine());

        // POST: Procesar el cambio de contraseña
        post("/profile/password", (req, res) -> {
            
            // 1. Verificación de seguridad: ¿Está logueado?
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return "";
            }

            // Obtener el ID del usuario actual desde la sesión
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return "";
            }

            // 2. Obtener los datos que el usuario escribió en el formulario web
            String currentPassword = req.queryParams("current_password");
            String newPassword = req.queryParams("new_password");
            String confirmPassword = req.queryParams("confirm_password");

            // 3. Validaciones de campos vacíos
            if (currentPassword == null || currentPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
                String msg = "Todos los campos son obligatorios.";
                res.redirect("/profile/password?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }

            // 4. Validar que la nueva contraseña y la confirmación sean exactamente iguales
            if (!newPassword.equals(confirmPassword)) {
                String msg = "Las nuevas contraseñas no coinciden.";
                res.redirect("/profile/password?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
            
            // 5. Validar la longitud mínima de seguridad
            if (newPassword.length() < 6) {
                String msg = "La nueva contraseña debe tener al menos 6 caracteres.";
                res.redirect("/profile/password?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }

            try {
                // 6. Buscar al usuario real en la base de datos
                User usuario = User.findById(userId);
                
                if (usuario != null) {
                    // 7. Obtener la contraseña encriptada vieja de la base de datos
                    String storedHashedPassword = usuario.getString("password");
                    
                    // 8. BCrypt comprueba si la "Actual" que escribió coincide con la encriptada
                    if (BCrypt.checkpw(currentPassword, storedHashedPassword)) {
                        
                        // Si es correcta, encriptamos la NUEVA contraseña y la guardamos
                        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                        usuario.set("password", newHashedPassword);
                        usuario.saveIt();
                        
                        // Redirigir con mensaje de éxito (Cartel Verde)
                        String msg = "Contraseña actualizada correctamente.";
                        res.redirect("/profile/password?message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                        return "";
                        
                    } else {
                        // Si la contraseña actual que ingresó no es la correcta (Cartel Rojo)
                        String msg = "La contraseña actual es incorrecta.";
                        res.redirect("/profile/password?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                        return "";
                    }
                } else {
                    res.redirect("/login");
                    return "";
                }

            } catch (Exception e) {
                System.err.println("Error al cambiar la contraseña: " + e.getMessage());
                String msg = "Error interno al intentar cambiar la contraseña.";
                res.redirect("/profile/password?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return "";
            }
        });

        // POST: Eliminar usuario desde Opciones Avanzadas
        post("/admin/usuarios/borrar", (req, res) -> {
            
            // 1. Verificación de seguridad estricta
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador");
            
            if (loggedIn == null || !loggedIn || esAdministrador == null || !esAdministrador) {
                res.redirect("/dashboard");
                return "";
            }

            // 2. Obtener el ID del usuario a eliminar y el ID del admin que está usando el sistema
            String userIdStr = req.queryParams("user_id");
            Integer currentAdminId = req.session().attribute("userId");

            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    int userToDeleteId = Integer.parseInt(userIdStr);

                    // 3. MEDIDA DE SEGURIDAD: Evitar que el administrador se borre a sí mismo
                    if (currentAdminId != null && currentAdminId == userToDeleteId) {
                        String errorMsg = "Medida de seguridad: No puedes eliminar tu propia cuenta de administrador.";
                        res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                        return "";
                    }

                    // 4. Buscar el usuario en la base de datos
                    User usuarioAEliminar = User.findById(userToDeleteId);

                    if (usuarioAEliminar != null) {
                        String nombreUsuario = usuarioAEliminar.getString("name");

                        // 5. Verificar si es Profesor y borrar esos datos PRIMERO (para evitar conflictos de la base de datos)
                        Profesores profeAsociado = Profesores.findById(userToDeleteId);
                        if (profeAsociado != null) {
                            profeAsociado.delete();
                        }

                        // 6. Eliminar finalmente el usuario
                        usuarioAEliminar.delete();

                        // 7. Redirigir con mensaje de éxito (Cartel verde)
                        String exitoMsg = "El usuario '" + nombreUsuario + "' y todos sus datos fueron eliminados correctamente.";
                        res.redirect("/admin/usuarios?message=" + URLEncoder.encode(exitoMsg, StandardCharsets.UTF_8));
                        return "";
                    } else {
                        String errorMsg = "El usuario seleccionado no existe en la base de datos.";
                        res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                        return "";
                    }

                } catch (Exception e) {
                    System.err.println("Error al intentar borrar usuario desde opciones avanzadas: " + e.getMessage());
                    String errorMsg = "Error interno al intentar eliminar el usuario.";
                    res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                    return "";
                }
            }

            // Si el ID llega nulo (no seleccionó nada en la lista)
            String errorMsg = "Por favor, seleccione un usuario válido de la lista desplegable.";
            res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            return "";
        });

        // POST: Hacer Administrador a un usuario
        post("/admin/usuarios/hacer-admin", (req, res) -> {
            
            // 1. Verificación de seguridad estricta
            Boolean loggedIn = req.session().attribute("loggedIn");
            Boolean esAdministrador = req.session().attribute("esAdministrador");
            
            if (loggedIn == null || !loggedIn || esAdministrador == null || !esAdministrador) {
                res.redirect("/dashboard");
                return "";
            }

            // 2. Obtener el ID del usuario seleccionado
            String userIdStr = req.queryParams("admin_user_id");

            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    int newAdminId = Integer.parseInt(userIdStr);

                    // 3. Buscar el usuario en la base de datos
                    User usuarioParaAdmin = User.findById(newAdminId);

                    if (usuarioParaAdmin != null) {
                        
                        // 4. Cambiar su estado a Administrador (esAdministrador = 1)
                        usuarioParaAdmin.set("esAdministrador", 1);
                        usuarioParaAdmin.saveIt();
                        
                        String nombreUsuario = usuarioParaAdmin.getString("name");

                        // 5. Redirigir con mensaje de éxito
                        String exitoMsg = "El usuario '" + nombreUsuario + "' ahora es Administrador del sistema.";
                        res.redirect("/admin/usuarios?message=" + URLEncoder.encode(exitoMsg, StandardCharsets.UTF_8));
                        return "";
                    } else {
                        String errorMsg = "El usuario seleccionado no existe.";
                        res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                        return "";
                    }

                } catch (Exception e) {
                    System.err.println("Error al hacer administrador a un usuario: " + e.getMessage());
                    String errorMsg = "Error interno al intentar asignar permisos.";
                    res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                    return "";
                }
            }

            // Si el ID llega nulo
            String errorMsg = "Por favor, seleccione un usuario válido.";
            res.redirect("/admin/usuarios?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            return "";
        });

        // POST: Guardar la nueva clase en la base de datos
        post("/profesor/clases/nueva", (req, res) -> {
            
            // 1. Verificación de seguridad
            if (req.session().attribute("loggedIn") == null) {
                res.redirect("/login");
                return "";
            }
            Integer userId = req.session().attribute("userId");
            if (Profesores.findById(userId) == null) {
                res.redirect("/dashboard");
                return "";
            }

            // 2. Recibir datos del formulario
            String nombre = req.queryParams("nombre");
            String descripcion = req.queryParams("descripcion");

            // 3. Validación básica
            if (nombre == null || nombre.trim().isEmpty() || descripcion == null || descripcion.trim().isEmpty()) {
                String errorMsg = "Todos los campos son obligatorios.";
                res.redirect("/profesor/clases/nueva?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                return "";
            }

            try {
                // 4. Crear la clase usando tu nuevo modelo
                Clase nuevaClase = new Clase();
                nuevaClase.setNombre(nombre.trim());
                nuevaClase.setDescripcion(descripcion.trim());
                nuevaClase.setProfesorId(userId); // Asignamos esta clase al profesor que está logueado
                nuevaClase.saveIt();

                String exitoMsg = "La clase '" + nombre + "' fue creada exitosamente.";
                res.redirect("/profesor/clases/nueva?message=" + URLEncoder.encode(exitoMsg, StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                System.err.println("Error al crear clase: " + e.getMessage());
                String errorMsg = "Error interno al guardar la clase en la base de datos.";
                res.redirect("/profesor/clases/nueva?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
                return "";
            }
        });

        // POST: Endpoint para añadir usuarios (API que devuelve JSON, no HTML).
        // Advertencia: Esta ruta tiene un propósito diferente a las de formulario HTML.
        post("/add_users", (req, res) -> {
            res.type("application/json"); // Establece el tipo de contenido de la respuesta a JSON.

            // Obtiene los parámetros 'name' y 'password' de la solicitud.
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // --- Validaciones básicas ---
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                
                User newUser = new User(); // Crea una nueva instancia de tu modelo User.
                newUser.set("name", name); // Asigna el nombre al campo 'name'.
                newUser.set("password", password); // Asigna la contraseña al campo 'password'.
                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Created.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(
                        Map.of("message", "Usuario '" + name + "' registrado con exito.", "id", newUser.getId()));

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Internal Server Error.
                return objectMapper
                        .writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

        // POST: Agregar alumno a una clase
        post("/profesor/clases/alumnos/agregar", (req, res) -> {
         if (req.session().attribute("loggedIn") == null) {
             res.redirect("/login"); return "";
         }
         String claseIdStr = req.queryParams("clase_id");
         String userIdStr = req.queryParams("user_id");

         if (claseIdStr != null && userIdStr != null) {
             try {
                 Inscripcion ins = new Inscripcion();
                 ins.setClaseId(Integer.parseInt(claseIdStr));
                 ins.setUserId(Integer.parseInt(userIdStr));
                 ins.saveIt();
                 res.redirect("/profesor/clases/detalle?clase_id=" + claseIdStr + "&message=" + URLEncoder.encode("Alumno inscrito con éxito.", StandardCharsets.UTF_8));
                 return "";
             } catch (Exception e) {
                 res.redirect("/profesor/clases/detalle?clase_id=" + claseIdStr + "&error=" + URLEncoder.encode("Error al agregar alumno.", StandardCharsets.UTF_8));
                 return "";
             }
         }
         res.redirect("/profesor/clases/lista");
         return "";
     });

        // POST: Expulsar alumno de una clase
        post("/profesor/clases/alumnos/remover", (req, res) -> {
         if (req.session().attribute("loggedIn") == null) {
             res.redirect("/login"); return "";
         }
         String claseIdStr = req.queryParams("clase_id");
         String inscripcionIdStr = req.queryParams("inscripcion_id");

         if (claseIdStr != null && inscripcionIdStr != null) {
             try {
                 Inscripcion ins = Inscripcion.findById(Integer.parseInt(inscripcionIdStr));
                 if (ins != null) {
                     ins.delete();
                     res.redirect("/profesor/clases/detalle?clase_id=" + claseIdStr + "&message=" + URLEncoder.encode("Alumno retirado de la cursada.", StandardCharsets.UTF_8));
                     return "";
                 }
             } catch (Exception e) {
                 res.redirect("/profesor/clases/detalle?clase_id=" + claseIdStr + "&error=" + URLEncoder.encode("Error al remover alumno.", StandardCharsets.UTF_8));
                 return "";
             }
         }
         res.redirect("/profesor/clases/lista");
         return "";
     });

    } // Fin del método main
} // Fin de la clase App