package com.cipherquest.levels.database;

import com.cipherquest.levels.BaseLevel;

public class DatabaseLevel extends BaseLevel {

    private String schemaSQL;
    private String expectedQuery;
    private String task;

    public DatabaseLevel(int checkpointId) {
        super(checkpointId);
    }

    @Override
    public void loadChallenge() {
        setTitle("Database Recon I");
        setDescription("ดึงรายชื่อผู้ใช้ทั้งหมดในตาราง users");
        setHint("ใช้ SELECT เพื่อเลือกคอลัมน์ที่ต้องการ");
        this.task = "แสดง username และ email ของผู้ใช้ทุกคน";
        this.schemaSQL = "CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, email TEXT, role TEXT);";
        this.expectedQuery = "SELECT username, email FROM users";
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        return false;
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.SQL_WORKBENCH;
    }

    public String getSchemaSQL() {
        return schemaSQL;
    }

    public String getTask() {
        return task;
    }

    public String getExpectedQuery() {
        return expectedQuery;
    }
}
