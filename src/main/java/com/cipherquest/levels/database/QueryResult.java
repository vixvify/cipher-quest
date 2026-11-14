package com.cipherquest.levels.database;

import java.util.Collections;
import java.util.List;

public class QueryResult {

    private final boolean success;
    private final String errorMessage;
    private final List<String> columnNames;
    private final List<List<String>> rows;
    private final int rowCount;
    private final long executionTimeMs;

    public QueryResult(boolean success, String errorMessage, List<String> columnNames, List<List<String>> rows, int rowCount, long executionTimeMs) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.columnNames = columnNames != null ? columnNames : Collections.emptyList();
        this.rows = rows != null ? rows : Collections.emptyList();
        this.rowCount = rowCount;
        this.executionTimeMs = executionTimeMs;
    }

    public static QueryResult success(List<String> columnNames, List<List<String>> rows, long executionTimeMs) {
        int count = rows != null ? rows.size() : 0;
        return new QueryResult(true, null, columnNames, rows, count, executionTimeMs);
    }

    public static QueryResult error(String errorMessage, long executionTimeMs) {
        return new QueryResult(false, errorMessage, Collections.emptyList(), Collections.emptyList(), 0, executionTimeMs);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}

