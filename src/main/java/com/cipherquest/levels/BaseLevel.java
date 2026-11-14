package com.cipherquest.levels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseLevel implements Level {

    private final int checkpointId;
    private String title;
    private String description;
    private String hint;
    private List<String> hints;
    private int currentHintLevel;
    private String difficulty;
    private List<String> sqlConcepts;
    private boolean completed;
    private int attempts;
    private boolean hintUsed;

    protected BaseLevel(int checkpointId) {
        this.checkpointId = checkpointId;
        this.hints = new ArrayList<>();
        this.currentHintLevel = 0;
        this.difficulty = "Normal";
        this.sqlConcepts = new ArrayList<>();
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
            return Collections.unmodifiableList(hints);
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
        return Collections.unmodifiableList(sqlConcepts);
    }

    @Override
    public int getCheckpointId() {
        return checkpointId;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setHint(String hint) {
        this.hint = hint;
        this.hints = new ArrayList<>();
        if (hint != null) {
            this.hints.add(hint);
        }
    }

    protected void setHints(List<String> hints) {
        this.hints = hints != null ? new ArrayList<>(hints) : new ArrayList<>();
        this.hint = this.hints.isEmpty() ? null : this.hints.get(0);
    }

    protected void setDifficulty(String difficulty) {
        this.difficulty = difficulty != null ? difficulty : "Normal";
    }

    protected void setSqlConcepts(List<String> sqlConcepts) {
        this.sqlConcepts = sqlConcepts != null ? new ArrayList<>(sqlConcepts) : new ArrayList<>();
    }

    protected void recordAttempt() {
        attempts++;
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
