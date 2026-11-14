package com.cipherquest.levels;

public interface Level {

    void loadChallenge();

    boolean checkAnswer(String input);

    String getHint();

    boolean useHint();

    boolean hasUsedHint();

    int getAttempts();

    String getDescription();

    String getTitle();

    int getCheckpointId();

    void markCompleted();

    boolean isCompleted();

    String getSuccessFeedback();

    String getFailureFeedback();

    ScreenType getScreenType();

    enum ScreenType {
        PASSKEY,
        TERMINAL,
        SQL_WORKBENCH
    }
}
