package com.cipherquest.levels.database;

import com.cipherquest.levels.BaseLevel;
import com.cipherquest.levels.ChallengeDefinition;

import java.util.Collections;
import java.util.List;

public class DatabaseLevel extends BaseLevel implements AutoCloseable {

    private final SqlDatabaseEnvironment dbEnv;
    private String missionCode;
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
        int checkpoint = getCheckpointId();
        ChallengeDefinition challenge = checkpoint >= 3 && checkpoint <= 7
                ? ChallengeDefinition.load(checkpoint)
                : ChallengeDefinition.loadDefaultDatabase();
        loadCommonFields(challenge);
        missionCode = challenge.required("mission_code");
        task = challenge.required("task");
        requiredTables = challenge.list("required_tables");
        expectedQuery = challenge.required("expected_query");
        mainClueId = challenge.optional("main_clue_id");
        lastQueryResult = null;
        lastFeedback = "";
        dbEnv.initDatabase();
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
        return getDescription();
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
