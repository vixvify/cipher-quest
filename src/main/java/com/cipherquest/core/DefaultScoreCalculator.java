package com.cipherquest.core;

public class DefaultScoreCalculator implements ScoreCalculator {

    private static final int BASE_SCORE = 100;
    private static final int FIRST_TRY_BONUS = 50;
    private static final int NO_HINT_BONUS = 50;
    private static final int MISTAKE_PENALTY = 15;
    private static final int HINT_PENALTY = 30;
    private static final int MINIMUM_SCORE = 25;

    @Override
    public int calculateScore(int checkpointId, int attempts, boolean hintUsed) {
        int earned = BASE_SCORE;

        if (attempts <= 1) {
            earned += FIRST_TRY_BONUS;
        } else {
            int extraAttempts = attempts - 1;
            earned -= (extraAttempts * MISTAKE_PENALTY);
        }

        if (!hintUsed) {
            earned += NO_HINT_BONUS;
        } else {
            earned -= HINT_PENALTY;
        }

        return Math.max(MINIMUM_SCORE, earned);
    }
}
