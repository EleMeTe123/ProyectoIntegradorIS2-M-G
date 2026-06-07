BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS users_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL DEFAULT 'STUDENT' CHECK (rol IN ('ADMIN', 'PROFESSOR', 'STUDENT')),
    nombre TEXT,
    apellido TEXT,
    email TEXT UNIQUE,
    dni INTEGER UNIQUE,
    legajo INTEGER UNIQUE,
    cargo TEXT,
    direccion TEXT,
    telefono TEXT
);

INSERT INTO users_new (id, name, password, rol, nombre, apellido, email, dni, legajo, cargo, direccion, telefono)
SELECT
    u.id, u.name, u.password,
    CASE
        WHEN u.esAdministrador = 1 THEN 'ADMIN'
        WHEN p.id_prof IS NOT NULL THEN 'PROFESSOR'
        ELSE 'STUDENT'
    END,
    p.nombre, p.apellido, p.correo,
    p.dni, p.legajo, p.cargo, p.direccion,
    CAST(p.telefono AS TEXT)
FROM users u
LEFT JOIN professors p ON u.id = p.id_prof;

DROP TABLE IF EXISTS professors;
DROP TABLE users;
ALTER TABLE users_new RENAME TO users;

COMMIT;
