package com.is1.proyecto.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConfigSingletonTest {

    @Test
    void getInstanceReturnsSameInstance() {
        DBConfigSingleton first = DBConfigSingleton.getInstance();
        DBConfigSingleton second = DBConfigSingleton.getInstance();
        assertSame(first, second);
    }

    @Test
    void driverIsSqlite() {
        assertEquals("org.sqlite.JDBC", DBConfigSingleton.getInstance().getDriver());
    }

    @Test
    void dbUrlUsesSystemPropertyWhenAvailable() {
        String customUrl = "jdbc:sqlite:./target/test.db";
        System.setProperty("db.url", customUrl);
        try {
            assertEquals(customUrl, DBConfigSingleton.getInstance().getDbUrl());
        } finally {
            System.clearProperty("db.url");
        }
    }

    @Test
    void userIsEmpty() {
        assertEquals("", DBConfigSingleton.getInstance().getUser());
    }

    @Test
    void passIsEmpty() {
        assertEquals("", DBConfigSingleton.getInstance().getPass());
    }
}
