package com.cipherquest.levels.database;

import com.cipherquest.levels.BaseLevel;

/**
 * [INHERITANCE] ด่านที่ 3–7: SQL Database Query
 *
 * extends BaseLevel → ได้ title, description, hint, attempts ฟรี
 * override checkAnswer() → ตรวจสอบด้วยการรัน query กับ SQLite จำลอง (Polymorphism)
 *
 * UI ที่ใช้: SqlWorkbenchScreen
 */
public class DatabaseLevel extends BaseLevel {

    // [ENCAPSULATION] ซ่อน schema และ expected result ไว้ภายใน
    private String schemaSQL;
    private String expectedQuery;
    private String task;

    public DatabaseLevel(int checkpointId) {
        super(checkpointId);
    }

    /**
     * [POLYMORPHISM] โหลดโจทย์เฉพาะของ SQL
     * TODO: อ่านจาก challenge_0X_db.yaml ตาม checkpointId
     */
    @Override
    public void loadChallenge() {
        // TODO: โหลดจาก YAML ตาม checkpointId (3–7)
        // ตัวอย่าง default สำหรับด่าน 3
        setTitle("Database Recon I");
        setDescription("ดึงรายชื่อผู้ใช้ทั้งหมดในตาราง users");
        setHint("ใช้ SELECT เพื่อเลือกคอลัมน์ที่ต้องการ");
        this.task = "แสดง username และ email ของผู้ใช้ทุกคน";
        this.schemaSQL = "CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, email TEXT, role TEXT);";
        this.expectedQuery = "SELECT username, email FROM users";
    }

    /**
     * [POLYMORPHISM] ตรวจสอบแบบ Database: รัน query จริงกับ SQLite in-memory
     * และเปรียบเทียบ result set กับเฉลย (ไม่ตรวจ syntax แต่ตรวจผลลัพธ์)
     * TODO: implement การรัน SQLite และเปรียบเทียบผลลัพธ์
     */
    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        // TODO: รัน input เป็น SQL query กับ SQLite แล้วตรวจผลลัพธ์
        return false;
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.SQL_WORKBENCH;
    }

    // Getter สำหรับ SqlWorkbenchController
    public String getSchemaSQL() { return schemaSQL; }
    public String getTask() { return task; }
    public String getExpectedQuery() { return expectedQuery; }
}
