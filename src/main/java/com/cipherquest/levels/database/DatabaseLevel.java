package com.cipherquest.levels.database;

import com.cipherquest.levels.BaseLevel;

import java.util.Collections;
import java.util.List;

public class DatabaseLevel extends BaseLevel implements AutoCloseable {

    private final SqlDatabaseEnvironment dbEnv;
    private String missionCode;
    private String story;
    private String task;
    private List<String> requiredTables;
    private String expectedQuery;
    private String mainClueId;
    private QueryResult lastQueryResult;
    private String lastFeedback;

    public DatabaseLevel(int checkpointId) {
        super(checkpointId);
        this.dbEnv = new SqlDatabaseEnvironment(false);
        this.requiredTables = Collections.emptyList();
        this.lastFeedback = "";
    }

    @Override
    public void loadChallenge() {
        resetProgressState();
        this.lastQueryResult = null;
        this.lastFeedback = "";
        dbEnv.initDatabase();
        int cp = getCheckpointId();

        switch (cp) {
            case 3 -> {
                this.missionCode = "M01";
                setTitle("M01 — นักสำรวจที่หายตัวไป");
                setDifficulty("ง่าย");
                setSqlConcepts(List.of("SELECT", "FROM", "WHERE"));
                this.requiredTables = List.of("explorers");
                this.story = "คุณเปิดใช้งานเทอร์มินัลเทคโนโลยีโบราณและเข้าสู่คลังข้อมูลได้สำเร็จ "
                        + "ข้อมูลชุดแรกที่พบคือรายชื่อนักสำรวจที่เคยเข้ามาสำรวจพื้นที่นี้ "
                        + "ลองตามหาข้อมูลล่าสุดของนักสำรวจที่ชื่อ Arin เพื่อดูเบาะแสแรกกัน";
                setDescription(this.story);
                this.task = "ค้นหาข้อมูลของนักสำรวจชื่อ Arin (explorer_id, explorer_name, status, clue)";
                setHints(List.of(
                        "ระดับ 1 (แนวคิด): ใช้ SELECT เลือกคอลัมน์ที่ต้องการ จากตารางด้วย FROM และกรองแถวด้วย WHERE เมื่อข้อความตรงกันพอดี",
                        "ระดับ 2 (โครงสร้าง): SELECT col1, col2, col3, col4 FROM table_name WHERE column_name = 'value';",
                        "ระดับ 3 (เจาะจง): เลือก explorer_id, explorer_name, status, clue จากตาราง explorers โดยใส่เงื่อนไข WHERE explorer_name = 'Arin'"
                ));
                this.expectedQuery = "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'";
                this.mainClueId = "MC01";
            }
            case 4 -> {
                this.missionCode = "M02";
                setTitle("M02 — ข้อความสุดท้าย");
                setDifficulty("ง่าย–ปานกลาง");
                setSqlConcepts(List.of("SELECT", "WHERE", "AND", "LIKE"));
                this.requiredTables = List.of("expedition_logs");
                this.story = "คุณเปิดดูบันทึกการเดินทางของ Arin ได้แล้ว "
                        + "บันทึกส่วนใหญ่เป็นข้อมูลทั่วไป แต่มีข้อความหนึ่งที่พูดถึงสัญลักษณ์ลึกลับ";
                setDescription(this.story);
                this.task = "ค้นหาบันทึกของ Arin ที่มีคำว่า 'สัญลักษณ์ (symbol)' (log_id, explorer_id, message, clue)";
                setHints(List.of(
                        "ระดับ 1 (แนวคิด): ใช้ WHERE ... AND ... เพื่อกรอง 2 เงื่อนไขพร้อมกัน และใช้ LIKE กับ '%' เพื่อค้นหาคำบางส่วนในข้อความ",
                        "ระดับ 2 (โครงสร้าง): SELECT col1, col2, col3, col4 FROM table_name WHERE id_col = 'ID' AND text_col LIKE '%keyword%';",
                        "ระดับ 3 (เจาะจง): เลือก log_id, explorer_id, message, clue จากตาราง expedition_logs โดยกำหนด explorer_id = 'E001' และ message LIKE '%symbol%'"
                ));
                this.expectedQuery = "SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE explorer_id = 'E001' AND message LIKE '%symbol%'";
                this.mainClueId = "MC02";
            }
            case 5 -> {
                this.missionCode = "M03";
                setTitle("M03 — โบราณวัตถุที่ถูกลืม");
                setDifficulty("ปานกลาง");
                setSqlConcepts(List.of("SELECT", "WHERE", "BETWEEN", "ORDER BY", "LIMIT"));
                this.requiredTables = List.of("artifacts");
                this.story = "คุณเดินเข้าสู่ห้องลับตามเบาะแสที่ได้มา ภายในห้องมีโบราณวัตถุบันทึกอยู่หลายชิ้น "
                        + "แต่มีเพียงชิ้นที่มีระดับพลังอยู่ในช่วงสำคัญเท่านั้นที่น่าตรวจสอบ";
                setDescription(this.story);
                this.task = "ค้นหาโบราณวัตถุในห้องจากเบาะแสเดิม ที่มีพลัง 50-100 สูงสุด 5 อันดับแรก (artifact_id, artifact_name, artifact_type, power_level, clue)";
                setHints(List.of(
                        "ระดับ 1 (แนวคิด): ใช้ BETWEEN เพื่อกรองช่วงตัวเลข ใช้ ORDER BY ... DESC เรียงจากมากไปน้อย และใช้ LIMIT จำกัดจำนวนแถว",
                        "ระดับ 2 (โครงสร้าง): SELECT ... FROM table_name WHERE loc_col = '...' AND num_col BETWEEN min AND max ORDER BY num_col DESC LIMIT n;",
                        "ระดับ 3 (เจาะจง): เลือก artifact_id, artifact_name, artifact_type, power_level, clue จาก artifacts โดยกรอง location_id = 'L002' และ power_level BETWEEN 50 AND 100 เรียงด้วย ORDER BY power_level DESC แล้ว LIMIT 5"
                ));
                this.expectedQuery = "SELECT artifact_id, artifact_name, artifact_type, power_level, clue FROM artifacts WHERE location_id = 'L002' AND power_level BETWEEN 50 AND 100 ORDER BY power_level DESC LIMIT 5";
                this.mainClueId = "MC03";
            }
            case 6 -> {
                this.missionCode = "M04";
                setTitle("M04 — บันทึกที่ถูกซ่อน");
                setDifficulty("ปานกลาง–ยาก");
                setSqlConcepts(List.of("SELECT", "JOIN", "ON", "WHERE"));
                this.requiredTables = List.of("artifacts", "experiments");
                this.story = "คุณพบว่าแกนพลัง Prime Core เคยถูกนำไปใช้ในการทดลองลับ "
                        + "ลองดึงบันทึกการทดลองที่เชื่อมโยงกับโบราณวัตถุชิ้นนี้ออกมาดู";
                setDescription(this.story);
                this.task = "ค้นหาข้อมูลการทดลองของ Prime Core โดยเชื่อมข้อมูลจาก 2 ตาราง (artifact_name, experiment_type, result, clue)";
                setHints(List.of(
                        "ระดับ 1 (แนวคิด): ใช้ JOIN ... ON เพื่อเชื่อมแถวจาก 2 ตารางเข้าด้วยกันผ่านคอลัมน์รหัสที่ตรงกัน",
                        "ระดับ 2 (โครงสร้าง): SELECT a.col1, e.col2, e.col3, e.col4 FROM table1 a JOIN table2 e ON a.id = e.id WHERE a.id = 'value';",
                        "ระดับ 3 (เจาะจง): เชื่อม artifacts a JOIN experiments e ON a.artifact_id = e.artifact_id แล้วกรอง WHERE a.artifact_id = 'A003' เพื่อเลือก artifact_name, experiment_type, result, clue"
                ));
                this.expectedQuery = "SELECT a.artifact_name, e.experiment_type, e.result, e.clue FROM artifacts a JOIN experiments e ON a.artifact_id = e.artifact_id WHERE a.artifact_id = 'A003'";
                this.mainClueId = "MC04";
            }
            case 7 -> {
                this.missionCode = "M05";
                setTitle("M05 — สมบัติที่สาบสูญ");
                setDifficulty("ท้าทาย");
                setSqlConcepts(List.of("SELECT", "JOIN", "ON", "WHERE", "AND", "Subquery", "MAX"));
                this.requiredTables = List.of("artifacts", "locations");
                this.story = "ในที่สุดคุณก็มาถึงห้องนิรภัยที่เบาะแสการทดลองก่อนหน้าชี้ไว้ "
                        + "ภารกิจสุดท้ายคือตามหาสมบัติที่ทรงพลังที่สุดที่ซ่อนอยู่ข้างใน";
                setDescription(this.story);
                this.task = "ค้นหา Treasure ที่พลังสูงสุดในห้องนิรภัยจากเบาะแสเดิม (artifact_name, artifact_type, location_name, power_level, clue)";
                setHints(List.of(
                        "ระดับ 1 (แนวคิด): ใช้ JOIN เชื่อมตารางสถานที่ ร่วมกับ Subquery และ MAX() ใน WHERE เพื่อหาชิ้นที่มีค่าพลังสูงสุด",
                        "ระดับ 2 (โครงสร้าง): SELECT ... FROM table1 a JOIN table2 l ON a.loc_id = l.loc_id WHERE a.loc_id = '...' AND a.type = '...' AND a.power = (SELECT MAX(power) FROM table1 WHERE ...);",
                        "ระดับ 3 (เจาะจง): เชื่อม artifacts a JOIN locations l ON a.location_id = l.location_id กรอง location_id = 'L005' AND artifact_type = 'Treasure' และให้ power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')"
                ));
                this.expectedQuery = "SELECT a.artifact_name, a.artifact_type, l.location_name, a.power_level, a.clue FROM artifacts a JOIN locations l ON a.location_id = l.location_id WHERE a.location_id = 'L005' AND a.artifact_type = 'Treasure' AND a.power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')";
                this.mainClueId = "MC05";
            }
            default -> {
                this.missionCode = "M00";
                setTitle("สำรวจคลังข้อมูลโบราณ");
                setDifficulty("ปกติ");
                setSqlConcepts(List.of("SELECT"));
                this.requiredTables = List.of("explorers");
                this.story = "สำรวจข้อมูลเบื้องต้นในฐานข้อมูล Ancient Archive";
                setDescription(this.story);
                this.task = "ดึงข้อมูลนักสำรวจทั้งหมดจากตาราง explorers";
                setHints(List.of("ใช้คำสั่ง SELECT * FROM explorers; เพื่อดูข้อมูลทั้งหมดในตาราง explorers"));
                this.expectedQuery = "SELECT * FROM explorers";
                this.mainClueId = null;
            }
        }
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        this.lastQueryResult = dbEnv.executeQuery(input);

        if (!lastQueryResult.isSuccess()) {
            this.lastFeedback = "ข้อผิดพลาด SQL (" + lastQueryResult.getErrorType() + "): " + lastQueryResult.getErrorMessage();
            return false;
        }

        boolean matched = dbEnv.evaluateAnswer(lastQueryResult, expectedQuery);
        if (matched) {
            this.lastFeedback = "ถูกต้อง! ผลลัพธ์ตรงตามเป้าหมายของด่าน (" + formatRowCount(lastQueryResult.getRowCount()) + ")";
            return true;
        } else {
            if (lastQueryResult.getRowCount() == 0) {
                this.lastFeedback = "ไม่พบข้อมูล: คำสั่งทำงานสำเร็จ (0 แถว) แต่ไม่มีข้อมูลที่ตรงตามเงื่อนไข";
            } else {
                this.lastFeedback = "ผลลัพธ์ยังไม่ถูกต้อง: คำสั่งทำงานสำเร็จ (" + formatRowCount(lastQueryResult.getRowCount()) + ") แต่ยังไม่ตรงกับเป้าหมายของด่าน";
            }
            return false;
        }
    }

    private String formatRowCount(int rowCount) {
        return rowCount + " แถว";
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

    public String getMissionCode() {
        return missionCode;
    }

    public String getStory() {
        return story;
    }

    public String getTask() {
        return task;
    }

    public List<String> getRequiredTables() {
        return requiredTables;
    }

    public String getExpectedQuery() {
        return expectedQuery;
    }

    public String getMainClueId() {
        return mainClueId;
    }

    public QueryResult getLastQueryResult() {
        return lastQueryResult;
    }

    public SqlDatabaseEnvironment getDbEnvironment() {
        if (!dbEnv.isInitialized()) {
            dbEnv.initDatabase();
        }
        return dbEnv;
    }

    @Override
    public void close() {
        dbEnv.close();
    }
}
