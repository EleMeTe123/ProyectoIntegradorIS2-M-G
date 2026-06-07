BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    professorId INTEGER NOT NULL,
    FOREIGN KEY (professorId) REFERENCES users(id)
);

INSERT INTO subjects (id, name, code, professorId)
SELECT id, nombre, UPPER(REPLACE(nombre, ' ', '_')), profesor_id
FROM clases;

CREATE TABLE IF NOT EXISTS registrations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    studentId INTEGER NOT NULL,
    subjectId INTEGER NOT NULL,
    UNIQUE(studentId, subjectId),
    FOREIGN KEY (studentId) REFERENCES users(id),
    FOREIGN KEY (subjectId) REFERENCES subjects(id)
);

INSERT INTO registrations (id, studentId, subjectId)
SELECT id, user_id, clase_id
FROM inscripciones;

DROP TABLE inscripciones;
DROP TABLE clases;

COMMIT;
