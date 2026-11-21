package com.cipherquest.core;

public class StrictScoreCalculator implements ScoreCalculator {

    private static final int BASE_SCORE = 100;
    private static final int HINT_PENALTY = 50;
    private static final int ATTEMPT_PENALTY = 25;

    @Override
    public int calculateScore(int checkpointId, int attempts, boolean hintUsed) {
        int earned = BASE_SCORE - ((Math.max(1, attempts) - 1) * ATTEMPT_PENALTY);
        if (hintUsed) {
            earned -= HINT_PENALTY;
        }
        return Math.max(0, earned);
    }
}
