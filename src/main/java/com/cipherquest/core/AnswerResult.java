package com.cipherquest.core;

/**
 * [ENCAPSULATION - การห่อหุ้มข้อมูล]
 * อ็อบเจกต์ผลลัพธ์การตรวจคำตอบ (Answer Evaluation Result)
 *
 * ห่อหุ้มรายละเอียดการตรวจคำตอบทั้งหมดไว้ในอ็อบเจกต์เดียว
 * ทำให้ GameEngine ส่งผลลัพธ์กลับไปยัง UI ได้อย่างชัดเจนโดยไม่ต้องแยกส่งทีละค่า
 */
public class AnswerResult {

    public enum Status {
        CORRECT,
        WRONG,
        GAME_OVER,
        GAME_CLEARED,
        ERROR
    }

    private final boolean correct;
    private final Status status;
    private final String message;
    private final int pointsAwarded;
    private final int remainingLives;
    private final int currentScore;

    public AnswerResult(boolean correct, Status status, String message, int pointsAwarded, int remainingLives, int currentScore) {
        this.correct = correct;
        this.status = status;
        this.message = message;
        this.pointsAwarded = pointsAwarded;
        this.remainingLives = remainingLives;
        this.currentScore = currentScore;
    }

    public static AnswerResult correct(String message, int pointsAwarded, int remainingLives, int currentScore) {
        return new AnswerResult(true, Status.CORRECT, message, pointsAwarded, remainingLives, currentScore);
    }

    public static AnswerResult wrong(String message, int remainingLives, int currentScore) {
        return new AnswerResult(false, Status.WRONG, message, 0, remainingLives, currentScore);
    }

    public static AnswerResult gameOver(String message, int currentScore) {
        return new AnswerResult(false, Status.GAME_OVER, message, 0, 0, currentScore);
    }

    public static AnswerResult gameCleared(String message, int pointsAwarded, int remainingLives, int currentScore) {
        return new AnswerResult(true, Status.GAME_CLEARED, message, pointsAwarded, remainingLives, currentScore);
    }

    public static AnswerResult error(String message) {
        return new AnswerResult(false, Status.ERROR, message, 0, 0, 0);
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

    public int getRemainingLives() {
        return remainingLives;
    }

    public int getCurrentScore() {
        return currentScore;
    }
}

