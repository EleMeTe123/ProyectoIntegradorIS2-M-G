package com.is1.proyecto.config;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public abstract class DatabaseTestBase {

    protected static final String DB_URL = System.getProperty("db.url", "jdbc:sqlite:./target/test.db");
    protected static final String DRIVER = "org.sqlite.JDBC";

    @BeforeEach
    void setUpDatabase() {
        Base.open(DRIVER, DB_URL, "", "");
        String schema = readSchema();
        Base.exec(schema);
    }

    @AfterEach
    void tearDownDatabase() {
        if (Base.hasConnection()) {
            Base.close();
        }
    }

    private String readSchema() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("scheme.sql");
        if (is == null) {
            throw new RuntimeException("scheme.sql not found in classpath");
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    protected void cleanTable(String table) {
        Base.exec("DELETE FROM " + table);
    }
}
