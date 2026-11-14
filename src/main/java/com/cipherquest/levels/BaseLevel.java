package com.cipherquest.levels;

public abstract class BaseLevel implements Level {

    private final int checkpointId;
    private String title;
    private String description;
    private String hint;
    private boolean completed;
    private int attempts;
    private boolean hintUsed;

    protected BaseLevel(int checkpointId) {
        this.checkpointId = checkpointId;
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
        return hint;
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
            return true;
        }
        return false;
    }

    @Override
    public boolean hasUsedHint() {
        return hintUsed;
    }

    @Override
    public abstract void loadChallenge();

    @Override
    public abstract boolean checkAnswer(String input);

    @Override
    public abstract ScreenType getScreenType();
}
