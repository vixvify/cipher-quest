package com.cipherquest.levels.database;

import java.sql.*;
import java.util.*;

public class SqlDatabaseEnvironment implements AutoCloseable {

    public static final int DEFAULT_QUERY_TIMEOUT_SECONDS = 5;
    public static final int DEFAULT_MAX_ROWS = 1000;

    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
            "CREATE", "PRAGMA", "ATTACH", "DETACH", "VACUUM"
    );

    private final int queryTimeoutSeconds;
    private final int maxRows;
    private Connection connection;
    private String initErrorMessage;

    public SqlDatabaseEnvironment() {
        this(true);
    }

    public SqlDatabaseEnvironment(boolean autoInit) {
        this(autoInit, DEFAULT_QUERY_TIMEOUT_SECONDS, DEFAULT_MAX_ROWS);
    }

    SqlDatabaseEnvironment(boolean autoInit, int queryTimeoutSeconds, int maxRows) {
        this.queryTimeoutSeconds = Math.max(1, queryTimeoutSeconds);
        this.maxRows = Math.max(1, maxRows);
        if (autoInit) {
            initDatabase();
        }
    }

    public boolean isInitialized() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void initDatabase() {
        initErrorMessage = null;
        try {
            close();
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }
            seedData();
            try (Statement readOnlyStmt = connection.createStatement()) {
                readOnlyStmt.execute("PRAGMA query_only = ON;");
            }
        } catch (SQLException e) {
            initErrorMessage = e.getMessage();
            close();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }

    private void seedData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE locations (" +
                    "location_id TEXT PRIMARY KEY, " +
                    "location_name TEXT NOT NULL, " +
                    "description TEXT NULL);");

            stmt.execute("INSERT INTO locations (location_id, location_name, description) VALUES " +
                    "('L001', 'Ancient Temple', 'Ancient surface ruins where Arin''s last signal was detected'), " +
                    "('L002', 'Relic Chamber', 'Inner chamber sealed by the Sun and Moon symbols'), " +
                    "('L003', 'Observatory Hall', 'Collapsed stargazing gallery with ancient stone carvings'), " +
                    "('L004', 'Research Sanctum', 'Restricted laboratory used for high-energy artifact testing'), " +
                    "('L005', 'Archive Vault', 'Deep subterranean vault housing the civilization''s final records');");

            stmt.execute("CREATE TABLE explorers (" +
                    "explorer_id TEXT PRIMARY KEY, " +
                    "explorer_name TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "clue TEXT NULL);");

            stmt.execute("INSERT INTO explorers (explorer_id, explorer_name, status, clue) VALUES " +
                    "('E001', 'Arin', 'Missing', 'Last signal detected near the Ancient Temple'), " +
                    "('E002', 'Lyra', 'Active', 'Surveying the outer perimeter of the ruins'), " +
                    "('E003', 'Kael', 'Retired', 'Compiled early translations of surface tablets'), " +
                    "('E004', 'Voss', 'Active', 'Maintaining communications relay at base camp');");

            stmt.execute("CREATE TABLE expedition_logs (" +
                    "log_id INTEGER PRIMARY KEY, " +
                    "explorer_id TEXT NOT NULL, " +
                    "location_id TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "clue TEXT NULL, " +
                    "FOREIGN KEY (explorer_id) REFERENCES explorers(explorer_id), " +
                    "FOREIGN KEY (location_id) REFERENCES locations(location_id));");

            stmt.execute("INSERT INTO expedition_logs (log_id, explorer_id, location_id, message, clue) VALUES " +
                    "(100, 'E001', 'L001', 'Arrived at the Ancient Temple entrance at dusk', 'Outer pillars match the archive map'), " +
                    "(101, 'E001', 'L001', 'The entrance requires two symbols', 'Sun and Moon symbols unlock the Relic Chamber (L002)'), " +
                    "(102, 'E001', 'L002', 'Inside the Relic Chamber; energy readings are off the charts', 'High-power artifacts detected on central pedestals'), " +
                    "(103, 'E002', 'L001', 'Found Arin''s discarded compass near the symbol gate', 'Check E001 expedition logs'), " +
                    "(104, 'E003', 'L003', 'Recorded celestial alignments in the Observatory Hall', NULL);");

            stmt.execute("CREATE TABLE inscriptions (" +
                    "inscription_id INTEGER PRIMARY KEY, " +
                    "symbol_type TEXT NOT NULL, " +
                    "inscription_text TEXT NOT NULL, " +
                    "location_id TEXT NOT NULL, " +
                    "clue TEXT NULL, " +
                    "FOREIGN KEY (location_id) REFERENCES locations(location_id));");

            stmt.execute("INSERT INTO inscriptions (inscription_id, symbol_type, inscription_text, location_id, clue) VALUES " +
                    "(201, 'Sun', 'The radiant disc awakens the chamber', 'L001', 'First key of the Relic Chamber'), " +
                    "(202, 'Moon', 'The silver crescent balances the flame', 'L001', 'Second key of the Relic Chamber');");

            stmt.execute("CREATE TABLE artifacts (" +
                    "artifact_id TEXT PRIMARY KEY, " +
                    "artifact_name TEXT NOT NULL, " +
                    "artifact_type TEXT NOT NULL, " +
                    "power_level INTEGER NOT NULL, " +
                    "location_id TEXT NOT NULL, " +
                    "clue TEXT NULL, " +
                    "FOREIGN KEY (location_id) REFERENCES locations(location_id));");

            stmt.execute("INSERT INTO artifacts (artifact_id, artifact_name, artifact_type, power_level, location_id, clue) VALUES " +
                    "('A001', 'Solar Core', 'Energy Core', 95, 'L002', 'Contains solar energy'), " +
                    "('A002', 'Moon Relic', 'Relic', 75, 'L002', 'Related to lunar energy'), " +
                    "('A003', 'Prime Core', 'Energy Core', 100, 'L002', 'Experimental records are restricted'), " +
                    "('A004', 'Cracked Conduit', 'Fragment', 20, 'L002', 'Power level too low to activate'), " +
                    "('A006', 'Gilded Chalice', 'Treasure', 40, 'L005', 'Ceremonial vessel of minor power'), " +
                    "('A007', 'Sovereign Diadem', 'Treasure', 70, 'L005', 'Royal emblem preserved in the vault'), " +
                    "('A008', 'Memory Archive', 'Treasure', 100, 'L005', 'Contains the memories of an ancient civilization'), " +
                    "('A009', 'Vault Sentinel Core', 'Guardian', 100, 'L005', 'Security matrix guarding the Archive Vault');");

            stmt.execute("CREATE TABLE experiments (" +
                    "experiment_id INTEGER PRIMARY KEY, " +
                    "experiment_type TEXT NOT NULL, " +
                    "artifact_id TEXT NOT NULL, " +
                    "result TEXT NULL, " +
                    "clue TEXT NULL, " +
                    "FOREIGN KEY (artifact_id) REFERENCES artifacts(artifact_id));");

            stmt.execute("INSERT INTO experiments (experiment_id, experiment_type, artifact_id, result, clue) VALUES " +
                    "(301, 'Solar Calibration', 'A001', 'Stable', 'Maintains steady thermal output'), " +
                    "(302, 'Lunar Harmonic Test', 'A002', 'Resonant', 'Responds to solar frequency'), " +
                    "(303, 'Energy Amplification', 'A003', 'Unknown', 'Unlocks Archive Vault at L005, containing the civilization''s final records');");
        }
    }

    public QueryResult executeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return QueryResult.error(
                    QueryResult.ErrorType.EMPTY_QUERY,
                    "Empty query: Please enter a SQL query.",
                    0
            );
        }

        AnalyzedSql analyzed = analyzeSql(sql);
        if (analyzed.hasUnterminatedBlockComment()) {
            if (!analyzed.tokens().isEmpty() && !isReadOnlyTokens(analyzed)) {
                return QueryResult.error(
                        QueryResult.ErrorType.INVALID_STATEMENT,
                        "Invalid SQL statement: Only read-only SELECT queries are permitted on the Ancient Archive.",
                        0
                );
            }
            return QueryResult.error(
                    QueryResult.ErrorType.SYNTAX_ERROR,
                    "Syntax error: Unterminated block comment (missing closing */).",
                    0
            );
        }

        if (analyzed.sanitizedSql().isEmpty()) {
            return QueryResult.error(
                    QueryResult.ErrorType.EMPTY_QUERY,
                    "Empty query: Please enter a SQL query.",
                    0
            );
        }

        if (!isReadOnlyTokens(analyzed)) {
            return QueryResult.error(
                    QueryResult.ErrorType.INVALID_STATEMENT,
                    "Invalid SQL statement: Only read-only SELECT queries are permitted on the Ancient Archive.",
                    0
            );
        }

        if (!isInitialized()) {
            String reason = initErrorMessage != null
                    ? "Database connection is not available: " + initErrorMessage
                    : "Database connection is not available.";
            return QueryResult.error(QueryResult.ErrorType.EXECUTION_ERROR, reason, 0);
        }

        long start = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(queryTimeoutSeconds);
            int rowFetchCap = maxRows == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxRows + 1;
            stmt.setMaxRows(rowFetchCap);

            try (ResultSet rs = stmt.executeQuery(analyzed.sanitizedSql())) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> colNames = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    String label = meta.getColumnLabel(i);
                    colNames.add(label != null && !label.isEmpty() ? label : meta.getColumnName(i));
                }

                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    if (rows.size() >= maxRows) {
                        return QueryResult.error(
                                QueryResult.ErrorType.EXECUTION_ERROR,
                                "Query execution failure: Result exceeded maximum limit of " + maxRows + " rows.",
                                System.currentTimeMillis() - start
                        );
                    }
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        row.add(val != null ? String.valueOf(val) : "NULL");
                    }
                    rows.add(row);
                }

                return QueryResult.success(colNames, rows, System.currentTimeMillis() - start);
            }
        } catch (SQLException e) {
            long elapsed = System.currentTimeMillis() - start;
            return classifySqlException(e, elapsed);
        }
    }

    private QueryResult classifySqlException(SQLException e, long elapsed) {
        String rawMsg = e.getMessage() != null ? e.getMessage() : "Unknown SQL error";
        String lower = rawMsg.toLowerCase(Locale.ROOT);

        if (lower.contains("no such table")) {
            return QueryResult.error(
                    QueryResult.ErrorType.UNKNOWN_TABLE,
                    "Unknown table: " + sanitizeErrorMessage(rawMsg),
                    elapsed
            );
        }
        if (lower.contains("no such column") || lower.contains("ambiguous column name")) {
            return QueryResult.error(
                    QueryResult.ErrorType.UNKNOWN_COLUMN,
                    "Unknown column: " + sanitizeErrorMessage(rawMsg),
                    elapsed
            );
        }
        if (lower.contains("syntax error")
                || lower.contains("incomplete input")
                || lower.contains("unrecognized token")) {
            return QueryResult.error(
                    QueryResult.ErrorType.SYNTAX_ERROR,
                    "Syntax error: " + sanitizeErrorMessage(rawMsg),
                    elapsed
            );
        }
        return QueryResult.error(
                QueryResult.ErrorType.EXECUTION_ERROR,
                "Query execution failure: " + sanitizeErrorMessage(rawMsg),
                elapsed
        );
    }

    private String sanitizeErrorMessage(String rawMsg) {
        String cleaned = rawMsg.replace("[SQLITE_ERROR] SQL error or missing database (", "");
        if (cleaned.endsWith(")") && !rawMsg.equals(cleaned)) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned.trim();
    }

    private record AnalyzedSql(
            String sanitizedSql,
            List<String> tokens,
            boolean hasInternalSemicolon,
            boolean hasUnterminatedBlockComment
    ) {}

    private AnalyzedSql analyzeSql(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int n = sql.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        boolean inBracket = false;
        boolean seenSemicolon = false;
        boolean hasInternalSemicolon = false;

        for (int i = 0; i < n; i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < n) ? sql.charAt(i + 1) : '\0';

            if (inSingleQuote) {
                out.append(c);
                if (c == '\'') {
                    if (next == '\'') {
                        out.append(next);
                        i++;
                    } else {
                        inSingleQuote = false;
                    }
                }
            } else if (inDoubleQuote) {
                out.append(c);
                if (c == '"') {
                    if (next == '"') {
                        out.append(next);
                        i++;
                    } else {
                        inDoubleQuote = false;
                    }
                }
            } else if (inBacktick) {
                out.append(c);
                if (c == '`') {
                    if (next == '`') {
                        out.append(next);
                        i++;
                    } else {
                        inBacktick = false;
                    }
                }
            } else if (inBracket) {
                out.append(c);
                if (c == ']') {
                    inBracket = false;
                }
            } else if (c == '-' && next == '-') {
                flushToken(currentToken, tokens);
                i += 2;
                while (i < n && sql.charAt(i) != '\n') {
                    i++;
                }
                if (i < n) {
                    out.append('\n');
                }
            } else if (c == '/' && next == '*') {
                flushToken(currentToken, tokens);
                int closeIdx = sql.indexOf("*/", i + 2);
                if (closeIdx < 0) {
                    return new AnalyzedSql("", tokens, hasInternalSemicolon, true);
                }
                i = closeIdx + 1;
                out.append(' ');
            } else {
                if (c == ';') {
                    flushToken(currentToken, tokens);
                    seenSemicolon = true;
                    out.append(c);
                    continue;
                }
                if (seenSemicolon && !Character.isWhitespace(c)) {
                    hasInternalSemicolon = true;
                }
                out.append(c);
                if (c == '\'') {
                    flushToken(currentToken, tokens);
                    inSingleQuote = true;
                } else if (c == '"') {
                    flushToken(currentToken, tokens);
                    inDoubleQuote = true;
                } else if (c == '`') {
                    flushToken(currentToken, tokens);
                    inBacktick = true;
                } else if (c == '[') {
                    flushToken(currentToken, tokens);
                    inBracket = true;
                } else if (Character.isLetterOrDigit(c) || c == '_') {
                    currentToken.append(Character.toUpperCase(c));
                } else {
                    flushToken(currentToken, tokens);
                }
            }
        }

        flushToken(currentToken, tokens);
        String sanitized = out.toString().trim();
        while (!hasInternalSemicolon && sanitized.endsWith(";")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1).trim();
        }
        return new AnalyzedSql(sanitized, tokens, hasInternalSemicolon, false);
    }

    private boolean isReadOnlyTokens(AnalyzedSql analyzed) {
        List<String> tokens = analyzed.tokens();
        if (analyzed.hasInternalSemicolon() || tokens.isEmpty()) {
            return false;
        }
        String first = tokens.get(0);
        if (!"SELECT".equals(first) && !"WITH".equals(first)) {
            return false;
        }
        for (String token : tokens) {
            if (FORBIDDEN_KEYWORDS.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private void flushToken(StringBuilder currentToken, List<String> tokens) {
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
            currentToken.setLength(0);
        }
    }

    public boolean evaluateAnswer(String userQuery, String expectedQuery) {
        QueryResult userResult = executeQuery(userQuery);
        return evaluateAnswer(userResult, expectedQuery);
    }

    public boolean evaluateAnswer(QueryResult userResult, String expectedQuery) {
        if (userResult == null || !userResult.isSuccess()) {
            return false;
        }

        QueryResult expectedResult = executeQuery(expectedQuery);
        if (!expectedResult.isSuccess()) {
            return false;
        }

        if (userResult.getColumnNames().size() != expectedResult.getColumnNames().size()) {
            return false;
        }

        if (userResult.getRowCount() != expectedResult.getRowCount()) {
            return false;
        }

        if (userResult.getRowCount() == 0 && expectedResult.getRowCount() == 0) {
            return true;
        }

        List<List<String>> uRows = userResult.getRows();
        List<List<String>> eRows = expectedResult.getRows();

        boolean checkOrdered = expectedQuery.toUpperCase(Locale.ROOT).contains("ORDER BY");
        if (checkOrdered) {
            for (int i = 0; i < uRows.size(); i++) {
                if (!compareRowValues(uRows.get(i), eRows.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            List<List<String>> uNormalized = normalizeRows(uRows);
            List<List<String>> eNormalized = normalizeRows(eRows);
            return uNormalized.equals(eNormalized);
        }
    }

    private boolean compareRowValues(List<String> r1, List<String> r2) {
        if (r1.size() != r2.size()) return false;
        List<String> sorted1 = new ArrayList<>(r1);
        List<String> sorted2 = new ArrayList<>(r2);
        Collections.sort(sorted1);
        Collections.sort(sorted2);
        return sorted1.equals(sorted2);
    }

    private List<List<String>> normalizeRows(List<List<String>> rows) {
        List<List<String>> normalized = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            List<String> copy = new ArrayList<>(row);
            Collections.sort(copy);
            normalized.add(copy);
        }
        normalized.sort(SqlDatabaseEnvironment::compareStringLists);
        return normalized;
    }

    private static int compareStringLists(List<String> a, List<String> b) {
        int minSize = Math.min(a.size(), b.size());
        for (int i = 0; i < minSize; i++) {
            int cmp = a.get(i).compareTo(b.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(a.size(), b.size());
    }

    public Connection getConnection() {
        return connection;
    }
}
