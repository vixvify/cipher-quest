package com.cipherquest.levels;

import java.util.List;

public interface Level {

    void loadChallenge();

    boolean checkAnswer(String input);

    String getHint();

    default List<String> getHints() {
        String hint = getHint();
        return hint != null ? List.of(hint) : List.of();
    }

    default int getHintLevel() {
        return hasUsedHint() ? 1 : 0;
    }

    default int getMaxHintLevel() {
        return getHints().size();
    }

    default String getDifficulty() {
        return "Normal";
    }

    default List<String> getSqlConcepts() {
        return List.of();
    }

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
