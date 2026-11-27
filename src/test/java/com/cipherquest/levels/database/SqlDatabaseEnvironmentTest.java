package com.cipherquest.levels.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlDatabaseEnvironmentTest {

    private SqlDatabaseEnvironment dbEnv;

    @BeforeEach
    void setUp() {
        dbEnv = new SqlDatabaseEnvironment();
    }

    @AfterEach
    void tearDown() {
        if (dbEnv != null) {
            dbEnv.close();
        }
    }

    @Test
    void testAllSixTablesExistAndForeignKeysEnabled() throws Exception {
        try (Statement stmt = dbEnv.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "PRAGMA foreign_keys should be enabled");
        }

        List<String> expectedTables = List.of(
                "locations", "explorers", "expedition_logs",
                "inscriptions", "artifacts", "experiments"
        );
        for (String table : expectedTables) {
            QueryResult res = dbEnv.executeQuery("SELECT * FROM " + table);
            assertTrue(res.isSuccess(), "Expected table to exist: " + table);
            assertTrue(res.getRowCount() > 0, "Expected seed rows in table: " + table);
        }
    }

    @Test
    void testMission01ExpectedQueryAndEvaluation() {
        String expected = "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'";

        QueryResult result = dbEnv.executeQuery(expected);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRowCount());
        assertEquals(List.of("E001", "Arin", "Missing", "พบสัญญาณล่าสุดใกล้ Ancient Temple"), result.getRows().get(0));

        assertTrue(dbEnv.evaluateAnswer(expected, expected));
        assertTrue(dbEnv.evaluateAnswer(
                "select clue, status, explorer_name, explorer_id from explorers e where e.explorer_name = 'Arin'",
                expected
        ));
        assertFalse(dbEnv.evaluateAnswer("SELECT * FROM explorers", expected));
        assertFalse(dbEnv.evaluateAnswer("SELECT explorer_id, explorer_name, status, clue FROM explorers", expected));
    }

    @Test
    void testMission02ExpectedQueryAndEvaluation() {
        String expected = "SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE explorer_id = 'E001' AND message LIKE '%symbol%'";

        QueryResult result = dbEnv.executeQuery(expected);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRowCount());
        assertEquals("101", result.getRows().get(0).get(0));
        assertEquals("E001", result.getRows().get(0).get(1));
        assertTrue(result.getRows().get(0).get(2).contains("symbol"));
        assertTrue(result.getRows().get(0).get(3).contains("L002"));

        assertTrue(dbEnv.evaluateAnswer(
                "SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE message LIKE '%symbol%' AND explorer_id = 'E001'",
                expected
        ));
        assertFalse(dbEnv.evaluateAnswer("SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE explorer_id = 'E001'", expected));
    }

    @Test
    void testMission03ExpectedQueryAndOrder() {
        String expected = "SELECT artifact_id, artifact_name, artifact_type, power_level, clue FROM artifacts WHERE location_id = 'L002' AND power_level BETWEEN 50 AND 100 ORDER BY power_level DESC LIMIT 5";

        QueryResult result = dbEnv.executeQuery(expected);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getRowCount());
        assertEquals("A003", result.getRows().get(0).get(0));
        assertEquals("A001", result.getRows().get(1).get(0));
        assertEquals("A002", result.getRows().get(2).get(0));

        assertTrue(dbEnv.evaluateAnswer(expected, expected));
        assertFalse(dbEnv.evaluateAnswer(
                "SELECT artifact_id, artifact_name, artifact_type, power_level, clue FROM artifacts WHERE location_id = 'L002' AND power_level BETWEEN 50 AND 100 ORDER BY power_level ASC LIMIT 5",
                expected
        ));
    }

    @Test
    void testMission04ExpectedQueryAndJoin() {
        String expected = "SELECT a.artifact_name, e.experiment_type, e.result, e.clue FROM artifacts a JOIN experiments e ON a.artifact_id = e.artifact_id WHERE a.artifact_id = 'A003'";

        QueryResult result = dbEnv.executeQuery(expected);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRowCount());
        assertEquals("Prime Core", result.getRows().get(0).get(0));
        assertEquals("Energy Amplification", result.getRows().get(0).get(1));
        assertEquals("Unknown", result.getRows().get(0).get(2));
        assertTrue(result.getRows().get(0).get(3).contains("L005"));
        assertFalse(dbEnv.evaluateAnswer(expected.replace("e.clue", "a.clue"), expected));

        assertTrue(dbEnv.evaluateAnswer(
                "SELECT artifacts.artifact_name, experiments.experiment_type, experiments.result, experiments.clue "
                        + "FROM artifacts INNER JOIN experiments ON artifacts.artifact_id = experiments.artifact_id "
                        + "WHERE artifacts.artifact_id = 'A003'",
                expected
        ));
    }

    @Test
    void testMission05ExpectedQueryAndSubquery() {
        String expected = "SELECT a.artifact_name, a.artifact_type, l.location_name, a.power_level, a.clue "
                + "FROM artifacts a JOIN locations l ON a.location_id = l.location_id "
                + "WHERE a.location_id = 'L005' AND a.artifact_type = 'Treasure' "
                + "AND a.power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')";

        QueryResult result = dbEnv.executeQuery(expected);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRowCount());
        assertEquals("Memory Archive", result.getRows().get(0).get(0));
        assertEquals("Treasure", result.getRows().get(0).get(1));
        assertEquals("Archive Vault", result.getRows().get(0).get(2));
        assertEquals("100", result.getRows().get(0).get(3));

        QueryResult treasures = dbEnv.executeQuery(
                "SELECT artifact_id, power_level FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure'"
        );
        assertTrue(treasures.isSuccess());
        assertTrue(treasures.getRowCount() >= 2, "L005 should contain at least 2 Treasure records");
    }

    @Test
    void testReadOnlySecurityBlocksMutations() {
        List<String> dangerousQueries = List.of(
                "DROP TABLE explorers",
                "DELETE FROM explorers WHERE explorer_id = 'E001'",
                "UPDATE explorers SET status = 'Found'",
                "INSERT INTO explorers VALUES ('E999', 'Hacker', 'Active', NULL)",
                "CREATE TABLE hack (id INT)",
                "ALTER TABLE explorers ADD COLUMN hacked TEXT",
                "PRAGMA table_info(explorers)",
                "SELECT * FROM explorers; DROP TABLE explorers;"
        );

        for (String query : dangerousQueries) {
            QueryResult res = dbEnv.executeQuery(query);
            assertFalse(res.isSuccess(), "Query must be blocked: " + query);
            assertEquals(QueryResult.ErrorType.INVALID_STATEMENT, res.getErrorType(), "Expected INVALID_STATEMENT for: " + query);
        }

        QueryResult safeLiteralQuery = dbEnv.executeQuery(
                "/* safe comment */ SELECT explorer_id FROM explorers WHERE clue NOT LIKE '%drop%' AND clue NOT LIKE '%update%';"
        );
        assertTrue(safeLiteralQuery.isSuccess());
        assertEquals(4, safeLiteralQuery.getRowCount());
    }

    @Test
    void testSqlErrorClassification() {
        QueryResult empty = dbEnv.executeQuery("   ");
        assertFalse(empty.isSuccess());
        assertEquals(QueryResult.ErrorType.EMPTY_QUERY, empty.getErrorType());

        QueryResult commentOnly = dbEnv.executeQuery("-- just a comment");
        assertFalse(commentOnly.isSuccess());
        assertEquals(QueryResult.ErrorType.EMPTY_QUERY, commentOnly.getErrorType());

        QueryResult syntax = dbEnv.executeQuery("SELECT * FORM explorers");
        assertFalse(syntax.isSuccess());
        assertEquals(QueryResult.ErrorType.SYNTAX_ERROR, syntax.getErrorType());

        QueryResult unknownTable = dbEnv.executeQuery("SELECT * FROM missing_table");
        assertFalse(unknownTable.isSuccess());
        assertEquals(QueryResult.ErrorType.UNKNOWN_TABLE, unknownTable.getErrorType());

        QueryResult unknownColumn = dbEnv.executeQuery("SELECT missing_col FROM explorers");
        assertFalse(unknownColumn.isSuccess());
        assertEquals(QueryResult.ErrorType.UNKNOWN_COLUMN, unknownColumn.getErrorType());

        QueryResult executionErr = dbEnv.executeQuery("SELECT 1 UNION SELECT 1, 2");
        assertFalse(executionErr.isSuccess());
        assertEquals(QueryResult.ErrorType.EXECUTION_ERROR, executionErr.getErrorType());
    }

    @Test
    void testQueryOnlyPragmaAndClosedConnectionSafety() throws Exception {
        try (Statement stmt = dbEnv.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA query_only;")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "PRAGMA query_only should be enabled");
        }

        dbEnv.close();
        QueryResult afterClose = dbEnv.executeQuery("SELECT * FROM explorers");
        assertFalse(afterClose.isSuccess());
        assertEquals(QueryResult.ErrorType.EXECUTION_ERROR, afterClose.getErrorType());
    }

    @Test
    void testReplaceFunctionWithCteAliasesAndDelimiterCollisionSafety() {
        QueryResult replaceQuery = dbEnv.executeQuery(
                "SELECT REPLACE(explorer_name, 'Arin', 'Arin [Missing]') AS label FROM explorers WHERE explorer_id = 'E001'"
        );
        assertTrue(replaceQuery.isSuccess());
        assertEquals("Arin [Missing]", replaceQuery.getRows().get(0).get(0));

        QueryResult cteQuery = dbEnv.executeQuery(
                "WITH arin AS (SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin') "
                        + "SELECT explorer_id, explorer_name, status, clue FROM arin"
        );
        assertTrue(cteQuery.isSuccess());
        assertTrue(dbEnv.evaluateAnswer(cteQuery,
                "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'"));

        assertTrue(dbEnv.evaluateAnswer(
                "SELECT explorer_id AS id, explorer_name AS name, status AS state, clue AS info FROM explorers WHERE explorer_name = 'Arin'",
                "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'"
        ));

        assertFalse(dbEnv.evaluateAnswer(
                "SELECT 'A||B', 'C'",
                "SELECT 'A', 'B||C'"
        ));

        QueryResult smuggled = dbEnv.executeQuery("SELECT * FROM explorers /* safe */ ; DROP TABLE explorers");
        assertFalse(smuggled.isSuccess());
        assertEquals(QueryResult.ErrorType.INVALID_STATEMENT, smuggled.getErrorType());
    }

    @Test
    void testUnterminatedCommentAndUnclosedQuoteReturnSyntaxError() {
        QueryResult unclosedCommentWithSelect = dbEnv.executeQuery("SELECT 1 /* unclosed");
        assertFalse(unclosedCommentWithSelect.isSuccess());
        assertEquals(QueryResult.ErrorType.SYNTAX_ERROR, unclosedCommentWithSelect.getErrorType());

        QueryResult unclosedCommentOnly = dbEnv.executeQuery("/* unclosed");
        assertFalse(unclosedCommentOnly.isSuccess());
        assertEquals(QueryResult.ErrorType.SYNTAX_ERROR, unclosedCommentOnly.getErrorType());

        QueryResult dropBeforeUnclosed = dbEnv.executeQuery("DROP TABLE explorers /* unclosed");
        assertFalse(dropBeforeUnclosed.isSuccess());
        assertEquals(QueryResult.ErrorType.INVALID_STATEMENT, dropBeforeUnclosed.getErrorType());

        QueryResult unclosedSingleQuote = dbEnv.executeQuery(
                "SELECT * FROM explorers WHERE explorer_name = 'Arin"
        );
        assertFalse(unclosedSingleQuote.isSuccess());
        assertEquals(QueryResult.ErrorType.SYNTAX_ERROR, unclosedSingleQuote.getErrorType());

        QueryResult unclosedDoubleQuote = dbEnv.executeQuery("SELECT \"unclosed FROM explorers");
        assertFalse(unclosedDoubleQuote.isSuccess());
        assertEquals(QueryResult.ErrorType.SYNTAX_ERROR, unclosedDoubleQuote.getErrorType());

        QueryResult trailingCommentAfterSemicolon = dbEnv.executeQuery("SELECT 1; -- trailing comment");
        assertTrue(trailingCommentAfterSemicolon.isSuccess());
        assertEquals(1, trailingCommentAfterSemicolon.getRowCount());

        QueryResult secondSelectAfterComment = dbEnv.executeQuery("SELECT 1; /* comment */ SELECT 2");
        assertFalse(secondSelectAfterComment.isSuccess());
        assertEquals(QueryResult.ErrorType.INVALID_STATEMENT, secondSelectAfterComment.getErrorType());
    }

    @Test
    void testRowLimitProtectionAndImmutability() {
        QueryResult withinLimit = dbEnv.executeQuery(
                "WITH RECURSIVE cnt(x) AS (SELECT 1 UNION ALL SELECT x + 1 FROM cnt LIMIT 1000) SELECT x FROM cnt"
        );
        assertTrue(withinLimit.isSuccess());
        assertEquals(1000, withinLimit.getRowCount());

        QueryResult exceedsDefaultLimit = dbEnv.executeQuery(
                "WITH RECURSIVE cnt(x) AS (SELECT 1 UNION ALL SELECT x + 1 FROM cnt LIMIT 1005) SELECT x FROM cnt"
        );
        assertFalse(exceedsDefaultLimit.isSuccess());
        assertEquals(QueryResult.ErrorType.EXECUTION_ERROR, exceedsDefaultLimit.getErrorType());

        QueryResult unboundedCte = dbEnv.executeQuery(
                "WITH RECURSIVE cnt(x) AS (SELECT 1 UNION ALL SELECT x + 1 FROM cnt) SELECT x FROM cnt"
        );
        assertFalse(unboundedCte.isSuccess());
        assertEquals(QueryResult.ErrorType.EXECUTION_ERROR, unboundedCte.getErrorType());

        try (SqlDatabaseEnvironment smallLimitEnv = new SqlDatabaseEnvironment(true, 1, 3)) {
            QueryResult limited = smallLimitEnv.executeQuery("SELECT explorer_id FROM explorers");
            assertFalse(limited.isSuccess());
            assertEquals(QueryResult.ErrorType.EXECUTION_ERROR, limited.getErrorType());
        }

        QueryResult immutableResult = dbEnv.executeQuery("SELECT explorer_id FROM explorers WHERE explorer_id = 'E001'");
        assertTrue(immutableResult.isSuccess());
        assertThrows(UnsupportedOperationException.class, () -> immutableResult.getColumnNames().add("extra"));
        assertThrows(UnsupportedOperationException.class, () -> immutableResult.getRows().add(List.of("extra")));
        assertThrows(UnsupportedOperationException.class, () -> immutableResult.getRows().get(0).add("extra"));

        QueryResult escapedBacktickAlias = dbEnv.executeQuery(
                "SELECT explorer_name AS `drop``;name` FROM explorers WHERE explorer_id = 'E001'"
        );
        assertTrue(escapedBacktickAlias.isSuccess());
        assertEquals("Arin", escapedBacktickAlias.getRows().get(0).get(0));
    }
}
