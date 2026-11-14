package com.cipherquest.player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Player {

    private final String username;
    private int score;
    private int hintsUsed;
    private final Set<Integer> completedCheckpoints;

    public Player(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.username = "Agent";
        } else {
            this.username = username.trim();
        }
        this.completedCheckpoints = new HashSet<>();
        reset();
    }

    public void reset() {
        this.score = 0;
        this.hintsUsed = 0;
        this.completedCheckpoints.clear();
    }

    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    public void recordHintUsed() {
        this.hintsUsed++;
    }

    public void markCheckpointCompleted(int checkpointId) {
        this.completedCheckpoints.add(checkpointId);
    }

    public boolean hasCompletedCheckpoint(int checkpointId) {
        return this.completedCheckpoints.contains(checkpointId);
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public Set<Integer> getCompletedCheckpoints() {
        return Collections.unmodifiableSet(completedCheckpoints);
    }
}
