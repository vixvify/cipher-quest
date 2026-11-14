package com.cipherquest.core;

public interface ScoreCalculator {

    int calculateScore(int checkpointId, int attempts, boolean hintUsed);
}
