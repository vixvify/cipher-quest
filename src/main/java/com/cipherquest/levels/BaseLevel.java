package com.cipherquest.levels;

import java.util.List;

public abstract class BaseLevel implements Level {

    private final int checkpointId;
    private String title;
    private String description;
    private String hint;
    private String endingMessage;
    private List<String> hints;
    private int currentHintLevel;
    private String difficulty;
    private List<String> sqlConcepts;
    private boolean completed;
    private int attempts;
    private boolean hintUsed;

    protected BaseLevel(int checkpointId) {
        this.checkpointId = checkpointId;
        this.hints = List.of();
        this.currentHintLevel = 0;
        this.difficulty = "Normal";
        this.sqlConcepts = List.of();
        this.completed = false;
        this.attempts = 0;
        this.hintUsed = false;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getHint() {
        if (!hints.isEmpty()) {
            int index = Math.min(Math.max(1, currentHintLevel), hints.size()) - 1;
            return hints.get(index);
        }
        return hint;
    }

    @Override
    public List<String> getHints() {
        if (!hints.isEmpty()) {
            return hints;
        }
        return hint != null ? List.of(hint) : List.of();
    }

    @Override
    public int getHintLevel() {
        return currentHintLevel;
    }

    @Override
    public int getMaxHintLevel() {
        return getHints().size();
    }

    @Override
    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public List<String> getSqlConcepts() {
        return sqlConcepts;
    }

    @Override
    public int getCheckpointId() {
        return checkpointId;
    }

    protected final void loadCommonFields(ChallengeDefinition challenge) {
        resetProgressState();
        title = challenge.required("title");
        description = challenge.required("description");
        hint = challenge.optional("hint");
        endingMessage = challenge.optional("ending_message");
        hints = challenge.list("hints");
        String configuredDifficulty = challenge.optional("difficulty");
        difficulty = configuredDifficulty == null ? "Normal" : configuredDifficulty;
        sqlConcepts = challenge.list("sql_concepts");
    }

    @Override
    public String getEndingMessage() {
        return endingMessage == null ? Level.super.getEndingMessage() : endingMessage;
    }

    protected void recordAttempt() {
        attempts++;
    }

    private void resetProgressState() {
        this.currentHintLevel = 0;
        this.attempts = 0;
        this.hintUsed = false;
        this.completed = false;
    }

    @Override
    public void markCompleted() {
        this.completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public int getAttempts() {
        return attempts;
    }

    @Override
    public boolean useHint() {
        if (!this.hintUsed) {
            this.hintUsed = true;
            this.currentHintLevel = 1;
            return true;
        }
        int maxLevel = getMaxHintLevel();
        if (this.currentHintLevel < maxLevel) {
            this.currentHintLevel++;
        }
        return false;
    }

    @Override
    public boolean hasUsedHint() {
        return hintUsed;
    }

    @Override
    public String getSuccessFeedback() {
        return "คำตอบถูกต้อง! ปลดล็อก Checkpoint " + checkpointId + " สำเร็จ";
    }

    @Override
    public String getFailureFeedback() {
        return "รหัสผ่านหรือคำสั่งไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง";
    }

    @Override
    public abstract void loadChallenge();

    @Override
    public abstract boolean checkAnswer(String input);

    @Override
    public abstract ScreenType getScreenType();
}
