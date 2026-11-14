package com.cipherquest.levels.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlDatabaseEnvironmentTest {

    private SqlDatabaseEnvironment dbEnv;

    @BeforeEach
    void setUp() {
        dbEnv = new SqlDatabaseEnvironment();
    }

    @Test
    void testExecuteSelectAllUsers() {
        QueryResult result = dbEnv.executeQuery("SELECT * FROM users");
        assertTrue(result.isSuccess());
        assertEquals(6, result.getRowCount());
        assertTrue(result.getColumnNames().contains("username"));
        assertTrue(result.getColumnNames().contains("email"));
    }

    @Test
    void testExecuteSyntaxError() {
        QueryResult result = dbEnv.executeQuery("SELECT * FORM users");
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void testEvaluateAnswerLevel3() {
        String expected = "SELECT username, email FROM users";

        assertFalse(dbEnv.evaluateAnswer("SELECT * FROM users", expected));
        assertFalse(dbEnv.evaluateAnswer("SELECT username FROM users", expected));

        assertTrue(dbEnv.evaluateAnswer("SELECT username, email FROM users", expected));
        assertTrue(dbEnv.evaluateAnswer("select email, username from users", expected));
        assertTrue(dbEnv.evaluateAnswer("SELECT u.username, u.email FROM users u", expected));
    }

    @Test
    void testEvaluateAnswerLevel4() {
        String expected = "SELECT username, email FROM users WHERE role = 'admin'";

        assertFalse(dbEnv.evaluateAnswer("SELECT username, email FROM users", expected));
        assertTrue(dbEnv.evaluateAnswer("SELECT username, email FROM users WHERE role = 'admin'", expected));
        assertTrue(dbEnv.evaluateAnswer("select email, username from users where role='admin'", expected));
    }

    @Test
    void testEvaluateAnswerLevel5Join() {
        String expected = "SELECT users.username FROM users JOIN access_logs ON users.id = access_logs.user_id WHERE access_logs.filename = 'classified.pdf'";

        assertTrue(dbEnv.evaluateAnswer(
                "SELECT u.username FROM users u INNER JOIN access_logs a ON u.id = a.user_id WHERE a.filename = 'classified.pdf'",
                expected
        ));
    }
}

