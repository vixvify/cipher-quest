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
                setTitle("M01 — The Missing Explorer");
                setDifficulty("Beginner");
                setSqlConcepts(List.of("SELECT", "FROM", "WHERE"));
                this.requiredTables = List.of("explorers");
                this.story = "The player activates the Ancient Technology Terminal and accesses the Ancient Archive. "
                        + "The first available records contain information about explorers who investigated the archaeological site. "
                        + "You must identify Arin and retrieve the last known information about the missing explorer.";
                setDescription(this.story);
                this.task = "Find Arin's explorer record (explorer_id, explorer_name, status, clue).";
                setHints(List.of(
                        "Level 1 (Concept): Use SELECT, FROM, and WHERE to retrieve specific columns and filter rows by an exact text value.",
                        "Level 2 (Structure): SELECT col1, col2, col3, col4 FROM table_name WHERE column_name = 'value';",
                        "Level 3 (Specific): Select explorer_id, explorer_name, status, and clue from the explorers table where explorer_name equals 'Arin'."
                ));
                this.expectedQuery = "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'";
                this.mainClueId = "MC01";
            }
            case 4 -> {
                this.missionCode = "M02";
                setTitle("M02 — The Last Message");
                setDifficulty("Beginner–Intermediate");
                setSqlConcepts(List.of("SELECT", "WHERE", "AND", "LIKE"));
                this.requiredTables = List.of("expedition_logs");
                this.story = "The player accesses the expedition logs associated with Arin. "
                        + "Most records contain ordinary expedition information, but one message mentions a mysterious symbol.";
                setDescription(this.story);
                this.task = "Find Arin's expedition message containing the word 'symbol' (log_id, explorer_id, message, clue).";
                setHints(List.of(
                        "Level 1 (Concept): Combine two filtering conditions using WHERE and AND, and use LIKE with '%' wildcards to search for a substring.",
                        "Level 2 (Structure): SELECT col1, col2, col3, col4 FROM table_name WHERE id_col = 'ID' AND text_col LIKE '%keyword%';",
                        "Level 3 (Specific): Select log_id, explorer_id, message, and clue from expedition_logs where explorer_id = 'E001' and message matches '%symbol%' using LIKE."
                ));
                this.expectedQuery = "SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE explorer_id = 'E001' AND message LIKE '%symbol%'";
                this.mainClueId = "MC02";
            }
            case 5 -> {
                this.missionCode = "M03";
                setTitle("M03 — The Forgotten Relics");
                setDifficulty("Intermediate");
                setSqlConcepts(List.of("SELECT", "WHERE", "BETWEEN", "ORDER BY", "LIMIT"));
                this.requiredTables = List.of("artifacts");
                this.story = "The player enters the chamber revealed by the previous clue. "
                        + "Several artifacts have been recorded there, but only those within a significant power range are worth investigating.";
                setDescription(this.story);
                this.task = "Find top 5 clue-location artifacts with power 50-100, highest first (artifact_id, artifact_name, artifact_type, power_level, clue).";
                setHints(List.of(
                        "Level 1 (Concept): Use BETWEEN for inclusive numeric ranges, ORDER BY ... DESC to sort highest to lowest, and LIMIT to cap the number of returned rows.",
                        "Level 2 (Structure): SELECT ... FROM table_name WHERE loc_col = '...' AND num_col BETWEEN min AND max ORDER BY num_col DESC LIMIT n;",
                        "Level 3 (Specific): Select artifact_id, artifact_name, artifact_type, power_level, and clue from artifacts where location_id = 'L002' and power_level is BETWEEN 50 AND 100, ordered by power_level DESC with LIMIT 5."
                ));
                this.expectedQuery = "SELECT artifact_id, artifact_name, artifact_type, power_level, clue FROM artifacts WHERE location_id = 'L002' AND power_level BETWEEN 50 AND 100 ORDER BY power_level DESC LIMIT 5";
                this.mainClueId = "MC03";
            }
            case 6 -> {
                this.missionCode = "M04";
                setTitle("M04 — The Hidden Records");
                setDifficulty("Intermediate–Advanced");
                setSqlConcepts(List.of("SELECT", "JOIN", "ON", "WHERE"));
                this.requiredTables = List.of("artifacts", "experiments");
                this.story = "The player discovers that the Prime Core was involved in a classified experiment. "
                        + "Uncover the hidden experiment records linked to it.";
                setDescription(this.story);
                this.task = "Retrieve the experiment associated with the Prime Core (artifact_name, experiment_type, result, clue).";
                setHints(List.of(
                        "Level 1 (Concept): Use JOIN ... ON to combine rows from two related tables based on a shared foreign key column.",
                        "Level 2 (Structure): SELECT a.col1, e.col2, e.col3, e.col4 FROM table1 a JOIN table2 e ON a.id = e.id WHERE a.id = 'value';",
                        "Level 3 (Specific): Join artifacts a and experiments e ON a.artifact_id = e.artifact_id WHERE a.artifact_id = 'A003', selecting artifact_name, experiment_type, result, clue."
                ));
                this.expectedQuery = "SELECT a.artifact_name, e.experiment_type, e.result, e.clue FROM artifacts a JOIN experiments e ON a.artifact_id = e.artifact_id WHERE a.artifact_id = 'A003'";
                this.mainClueId = "MC04";
            }
            case 7 -> {
                this.missionCode = "M05";
                setTitle("M05 — The Lost Treasure");
                setDifficulty("Advanced");
                setSqlConcepts(List.of("SELECT", "JOIN", "ON", "WHERE", "AND", "Subquery", "MAX"));
                this.requiredTables = List.of("artifacts", "locations");
                this.story = "The player finally reaches the vault revealed by the previous experiment clue. "
                        + "Track down the most powerful Treasure hidden inside it.";
                setDescription(this.story);
                this.task = "Find the highest-power Treasure inside the vault from the previous clue (artifact_name, artifact_type, location_name, power_level, clue).";
                setHints(List.of(
                        "Level 1 (Concept): Combine a JOIN between two tables with a WHERE subquery that uses the MAX() aggregate function to find the highest value.",
                        "Level 2 (Structure): SELECT ... FROM table1 a JOIN table2 l ON a.loc_id = l.loc_id WHERE a.loc_id = '...' AND a.type = '...' AND a.power = (SELECT MAX(power) FROM table1 WHERE ...);",
                        "Level 3 (Specific): Join artifacts a with locations l on location_id, filter by location_id = 'L005' AND artifact_type = 'Treasure', and match power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')."
                ));
                this.expectedQuery = "SELECT a.artifact_name, a.artifact_type, l.location_name, a.power_level, a.clue FROM artifacts a JOIN locations l ON a.location_id = l.location_id WHERE a.location_id = 'L005' AND a.artifact_type = 'Treasure' AND a.power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')";
                this.mainClueId = "MC05";
            }
            default -> {
                this.missionCode = "M00";
                setTitle("Ancient Archive Recon");
                setDifficulty("Normal");
                setSqlConcepts(List.of("SELECT"));
                this.requiredTables = List.of("explorers");
                this.story = "Explore the Ancient Archive database.";
                setDescription(this.story);
                this.task = "Retrieve explorer records from the Ancient Archive.";
                setHints(List.of("Use SELECT * FROM explorers; to inspect the explorers table."));
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
            this.lastFeedback = "SQL Error (" + lastQueryResult.getErrorType() + "): " + lastQueryResult.getErrorMessage();
            return false;
        }

        boolean matched = dbEnv.evaluateAnswer(lastQueryResult, expectedQuery);
        if (matched) {
            this.lastFeedback = "Query OK! Result matches mission objective (" + formatRowCount(lastQueryResult.getRowCount()) + ").";
            return true;
        } else {
            if (lastQueryResult.getRowCount() == 0) {
                this.lastFeedback = "Empty query result: Query executed successfully (0 rows), but returned no matching records.";
            } else {
                this.lastFeedback = "Incorrect result: Query executed successfully (" + formatRowCount(lastQueryResult.getRowCount()) + "), but does not match mission objective.";
            }
            return false;
        }
    }

    private String formatRowCount(int rowCount) {
        return rowCount == 1 ? "1 row" : rowCount + " rows";
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
