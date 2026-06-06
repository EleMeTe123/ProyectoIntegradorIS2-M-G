
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,

    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    rol TEXT NOT NULL CHECK (
        rol IN ('ADMIN','PROFESSOR','STUDENT')
        )
);

CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    professorId INTEGER NOT NULL,
    FOREIGN KEY (professorId)
        REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS registrations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    studentId INTEGER NOT NULL,
    subjectId INTEGER NOT NULL,

    UNIQUE(studentId, subjectId),

    FOREIGN KEY (studentId) REFERENCES users(id),
    FOREIGN KEY (subjectId) REFERENCES subjects(id)
);
