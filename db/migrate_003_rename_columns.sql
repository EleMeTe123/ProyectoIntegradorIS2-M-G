BEGIN TRANSACTION;

CREATE TABLE users_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userName TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL DEFAULT 'STUDENT' CHECK (rol IN ('ADMIN', 'PROFESSOR', 'STUDENT')),
    firstName TEXT,
    lastName TEXT,
    email TEXT UNIQUE,
    dni INTEGER UNIQUE,
    address TEXT,
    phoneNumber TEXT
);

INSERT INTO users_new (id, userName, password, rol, firstName, lastName, email, dni, address, phoneNumber)
SELECT id, name, password, rol, nombre, apellido, email, dni, direccion, telefono FROM users;

DROP TABLE users;
ALTER TABLE users_new RENAME TO users;

COMMIT;
