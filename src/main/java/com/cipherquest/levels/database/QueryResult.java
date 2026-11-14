package com.cipherquest.levels.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResult {

    public enum ErrorType {
        NONE,
        EMPTY_QUERY,
        INVALID_STATEMENT,
        SYNTAX_ERROR,
        UNKNOWN_TABLE,
        UNKNOWN_COLUMN,
        EXECUTION_ERROR
    }

    private final boolean success;
    private final ErrorType errorType;
    private final String errorMessage;
    private final List<String> columnNames;
    private final List<List<String>> rows;
    private final int rowCount;
    private final long executionTimeMs;

    public QueryResult(boolean success, String errorMessage, List<String> columnNames, List<List<String>> rows, int rowCount, long executionTimeMs) {
        this(success, success ? ErrorType.NONE : ErrorType.EXECUTION_ERROR, errorMessage, columnNames, rows, rowCount, executionTimeMs);
    }

    public QueryResult(boolean success, ErrorType errorType, String errorMessage, List<String> columnNames, List<List<String>> rows, int rowCount, long executionTimeMs) {
        this.success = success;
        this.errorType = errorType != null ? errorType : (success ? ErrorType.NONE : ErrorType.EXECUTION_ERROR);
        this.errorMessage = errorMessage;
        this.columnNames = columnNames != null
                ? Collections.unmodifiableList(new ArrayList<>(columnNames))
                : Collections.emptyList();
        if (rows != null) {
            List<List<String>> copiedRows = new ArrayList<>(rows.size());
            for (List<String> row : rows) {
                copiedRows.add(row != null
                        ? Collections.unmodifiableList(new ArrayList<>(row))
                        : Collections.emptyList());
            }
            this.rows = Collections.unmodifiableList(copiedRows);
        } else {
            this.rows = Collections.emptyList();
        }
        this.rowCount = rowCount;
        this.executionTimeMs = executionTimeMs;
    }

    public static QueryResult success(List<String> columnNames, List<List<String>> rows, long executionTimeMs) {
        int count = rows != null ? rows.size() : 0;
        return new QueryResult(true, ErrorType.NONE, null, columnNames, rows, count, executionTimeMs);
    }

    public static QueryResult error(String errorMessage, long executionTimeMs) {
        return error(ErrorType.EXECUTION_ERROR, errorMessage, executionTimeMs);
    }

    public static QueryResult error(ErrorType errorType, String errorMessage, long executionTimeMs) {
        ErrorType resolvedType = errorType != null ? errorType : ErrorType.EXECUTION_ERROR;
        return new QueryResult(false, resolvedType, errorMessage, Collections.emptyList(), Collections.emptyList(), 0, executionTimeMs);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorType getErrorType() {
        return errorType;
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

