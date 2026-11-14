package com.cipherquest.levels.database;

import java.sql.*;
import java.util.*;

public class SqlDatabaseEnvironment {

    private Connection connection;

    public SqlDatabaseEnvironment() {
        initDatabase();
    }

    public void initDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            seedData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "role TEXT NOT NULL);");

            stmt.execute("INSERT INTO users (id, username, email, role) VALUES " +
                    "(1, 'alice', 'alice@cipherquest.internal', 'admin'), " +
                    "(2, 'bob', 'bob@cipherquest.internal', 'developer'), " +
                    "(3, 'charlie', 'charlie@cipherquest.internal', 'analyst'), " +
                    "(4, 'david', 'david@cipherquest.internal', 'developer'), " +
                    "(5, 'eve', 'eve@cipherquest.internal', 'admin'), " +
                    "(6, 'mallory', 'mallory@cipherquest.internal', 'guest');");

            stmt.execute("CREATE TABLE access_logs (" +
                    "id INTEGER PRIMARY KEY, " +
                    "user_id INTEGER NOT NULL, " +
                    "filename TEXT NOT NULL, " +
                    "accessed_at TEXT NOT NULL);");

            stmt.execute("INSERT INTO access_logs (id, user_id, filename, accessed_at) VALUES " +
                    "(1, 1, 'classified.pdf', '2026-11-10 09:12:00'), " +
                    "(2, 2, 'source_code.zip', '2026-11-10 09:30:15'), " +
                    "(3, 5, 'classified.pdf', '2026-11-10 10:05:42'), " +
                    "(4, 1, 'system_config.yaml', '2026-11-10 11:20:00'), " +
                    "(5, 3, 'audit.log', '2026-11-10 12:45:10'), " +
                    "(6, 5, 'classified.pdf', '2026-11-10 13:10:05'), " +
                    "(7, 5, 'master_keys.enc', '2026-11-10 14:00:22'), " +
                    "(8, 2, 'readme.txt', '2026-11-10 15:30:00'), " +
                    "(9, 5, 'backup.sql', '2026-11-10 16:15:11');");

            stmt.execute("CREATE TABLE files (" +
                    "id INTEGER PRIMARY KEY, " +
                    "filename TEXT NOT NULL, " +
                    "file_size INTEGER NOT NULL, " +
                    "classification TEXT NOT NULL);");

            stmt.execute("INSERT INTO files (id, filename, file_size, classification) VALUES " +
                    "(1, 'classified.pdf', 1048576, 'TOP_SECRET'), " +
                    "(2, 'master_keys.enc', 4096, 'TOP_SECRET'), " +
                    "(3, 'source_code.zip', 5242880, 'CONFIDENTIAL'), " +
                    "(4, 'audit.log', 204800, 'INTERNAL'), " +
                    "(5, 'system_config.yaml', 8192, 'CONFIDENTIAL');");
        }
    }

    public QueryResult executeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return QueryResult.error("Query cannot be empty", 0);
        }

        long start = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement()) {
            boolean hasResultSet = stmt.execute(sql.trim());
            long elapsed = System.currentTimeMillis() - start;

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    List<String> colNames = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        String label = meta.getColumnLabel(i);
                        colNames.add(label != null && !label.isEmpty() ? label : meta.getColumnName(i));
                    }

                    List<List<String>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            row.add(val != null ? String.valueOf(val) : "NULL");
                        }
                        rows.add(row);
                    }

                    return QueryResult.success(colNames, rows, elapsed);
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                List<String> colNames = List.of("result");
                List<List<String>> rows = List.of(List.of(updateCount + " row(s) affected"));
                return QueryResult.success(colNames, rows, elapsed);
            }
        } catch (SQLException e) {
            long elapsed = System.currentTimeMillis() - start;
            return QueryResult.error(e.getMessage(), elapsed);
        }
    }

    public boolean evaluateAnswer(String userQuery, String expectedQuery) {
        QueryResult userResult = executeQuery(userQuery);
        if (!userResult.isSuccess()) {
            return false;
        }

        QueryResult expectedResult = executeQuery(expectedQuery);
        if (!expectedResult.isSuccess()) {
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

        boolean checkOrdered = expectedQuery.toUpperCase().contains("ORDER BY");
        if (checkOrdered) {
            for (int i = 0; i < uRows.size(); i++) {
                if (!compareRowValues(uRows.get(i), eRows.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            List<String> uSerialized = serializeRows(uRows);
            List<String> eSerialized = serializeRows(eRows);
            Collections.sort(uSerialized);
            Collections.sort(eSerialized);
            return uSerialized.equals(eSerialized);
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

    private List<String> serializeRows(List<List<String>> rows) {
        List<String> list = new ArrayList<>();
        for (List<String> row : rows) {
            List<String> copy = new ArrayList<>(row);
            Collections.sort(copy);
            list.add(String.join("||", copy));
        }
        return list;
    }
}

