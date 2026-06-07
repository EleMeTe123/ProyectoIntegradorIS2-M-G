-- Migration for prod.db (empty, no esAdministrador column)
BEGIN TRANSACTION;
DROP TABLE users;
CREATE TABLE users (
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
COMMIT;
