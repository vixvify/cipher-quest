package com.cipherquest.core;

public class AnswerResult {

    public enum Status {
        CORRECT,
        WRONG,
        GAME_CLEARED,
        ERROR
    }

    private final boolean correct;
    private final Status status;
    private final String message;
    private final int pointsAwarded;
    private final int currentScore;

    public AnswerResult(boolean correct, Status status, String message, int pointsAwarded, int currentScore) {
        this.correct = correct;
        this.status = status;
        this.message = message;
        this.pointsAwarded = pointsAwarded;
        this.currentScore = currentScore;
    }

    public static AnswerResult correct(String message, int pointsAwarded, int currentScore) {
        return new AnswerResult(true, Status.CORRECT, message, pointsAwarded, currentScore);
    }

    public static AnswerResult wrong(String message, int currentScore) {
        return new AnswerResult(false, Status.WRONG, message, 0, currentScore);
    }

    public static AnswerResult gameCleared(String message, int pointsAwarded, int currentScore) {
        return new AnswerResult(true, Status.GAME_CLEARED, message, pointsAwarded, currentScore);
    }

    public static AnswerResult error(String message) {
        return new AnswerResult(false, Status.ERROR, message, 0, 0);
    }

    public boolean isCorrect() {
        return correct;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public int getCurrentScore() {
        return currentScore;
    }
}
