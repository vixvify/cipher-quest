package com.cipherquest.levels.database;

import com.cipherquest.levels.BaseLevel;

public class DatabaseLevel extends BaseLevel {

    private final SqlDatabaseEnvironment dbEnv;
    private String task;
    private String expectedQuery;
    private QueryResult lastQueryResult;
    private String lastFeedback;

    public DatabaseLevel(int checkpointId) {
        super(checkpointId);
        this.dbEnv = new SqlDatabaseEnvironment();
        this.lastFeedback = "";
    }

    @Override
    public void loadChallenge() {
        dbEnv.initDatabase();
        int cp = getCheckpointId();

        switch (cp) {
            case 3 -> {
                setTitle("Database Recon I");
                setDescription("ดึงรายชื่อผู้ใช้ทั้งหมดในตาราง users");
                this.task = "แสดงคอลัมน์ username และ email ของผู้ใช้ทุกคนในตาราง users";
                setHint("ใช้คำสั่ง SELECT username, email FROM users;");
                this.expectedQuery = "SELECT username, email FROM users";
            }
            case 4 -> {
                setTitle("Database Recon II");
                setDescription("ค้นหาผู้ใช้ที่เป็น admin ในระบบ");
                this.task = "ดึงข้อมูล username และ email ของผู้ใช้ที่มี role = 'admin'";
                setHint("ใช้เงื่อนไข WHERE role = 'admin' เพื่อกรองเฉพาะผู้ใช้ที่เป็น admin");
                this.expectedQuery = "SELECT username, email FROM users WHERE role = 'admin'";
            }
            case 5 -> {
                setTitle("Database Recon III");
                setDescription("ตรวจสอบ log การเข้าถึงเอกสารลับ");
                this.task = "JOIN ตาราง users กับ access_logs แล้วดึง username ของคนที่เปิด classified.pdf";
                setHint("ใช้ JOIN access_logs ON users.id = access_logs.user_id WHERE filename = 'classified.pdf'");
                this.expectedQuery = "SELECT users.username FROM users JOIN access_logs ON users.id = access_logs.user_id WHERE access_logs.filename = 'classified.pdf'";
            }
            case 6 -> {
                setTitle("Database Recon IV");
                setDescription("สรุปสถิติความถี่การเข้าถึงระบบ");
                this.task = "นับจำนวนแถวใน access_logs ของแต่ละ user_id แล้วเรียงลำดับจากมากไปน้อย";
                setHint("ใช้ GROUP BY user_id ORDER BY COUNT(*) DESC");
                this.expectedQuery = "SELECT user_id, COUNT(*) as access_count FROM access_logs GROUP BY user_id ORDER BY COUNT(*) DESC";
            }
            case 7 -> {
                setTitle("Database Boss: Final Breach");
                setDescription("เจาะระบบขั้นสูงสุด ดึงข้อมูลเป้าหมายหลัก");
                this.task = "ค้นหา username ของ user ที่มีจำนวนการเข้าถึง (access_logs) มากที่สุดในระบบ";
                setHint("ใช้ Subquery หรือ JOIN เพื่อหา user_id ที่มี log มากที่สุด แล้วนำไปหา username ในตาราง users");
                this.expectedQuery = "SELECT username FROM users WHERE id = (SELECT user_id FROM access_logs GROUP BY user_id ORDER BY COUNT(*) DESC LIMIT 1)";
            }
            default -> {
                setTitle("Database Recon");
                setDescription("ภารกิจฐานข้อมูล");
                this.task = "สำรวจข้อมูลในฐานข้อมูล";
                setHint("ใช้คำสั่ง SELECT เพื่อดูตาราง");
                this.expectedQuery = "SELECT * FROM users";
            }
        }
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        this.lastQueryResult = dbEnv.executeQuery(input);

        if (!lastQueryResult.isSuccess()) {
            this.lastFeedback = "SQL Error: " + lastQueryResult.getErrorMessage();
            return false;
        }

        boolean matched = dbEnv.evaluateAnswer(input, expectedQuery);
        if (matched) {
            this.lastFeedback = "Query OK! Result matches mission objective (" + lastQueryResult.getRowCount() + " rows).";
            return true;
        } else {
            this.lastFeedback = "Query executed successfully (" + lastQueryResult.getRowCount() + " rows), but does not match mission objective.";
            return false;
        }
    }

    @Override
    public String getSuccessFeedback() {
        return lastFeedback != null && !lastFeedback.isEmpty()
                ? lastFeedback
                : super.getSuccessFeedback();
    }

    @Override
    public String getFailureFeedback() {
        return lastFeedback != null && !lastFeedback.isEmpty()
                ? lastFeedback
                : super.getFailureFeedback();
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.SQL_WORKBENCH;
    }

    public String getTask() {
        return task;
    }

    public String getExpectedQuery() {
        return expectedQuery;
    }

    public QueryResult getLastQueryResult() {
        return lastQueryResult;
    }

    public SqlDatabaseEnvironment getDbEnvironment() {
        return dbEnv;
    }
}
