package com.cipherquest.player;

import com.cipherquest.clues.Clue;
import com.cipherquest.clues.ClueArchive;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player {

    private final String username;
    private int score;
    private int hintsUsed;
    private final Set<Integer> completedCheckpoints;
    private final Set<Integer> unlockedCheckpoints;
    private final ClueArchive clueArchive;

    public Player(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.username = "Agent";
        } else {
            this.username = username.trim();
        }
        this.completedCheckpoints = new HashSet<>();
        this.unlockedCheckpoints = new HashSet<>();
        this.clueArchive = new ClueArchive();
        reset();
    }

    public void reset() {
        this.score = 0;
        this.hintsUsed = 0;
        this.completedCheckpoints.clear();
        this.unlockedCheckpoints.clear();
        this.unlockedCheckpoints.add(1);
        this.clueArchive.reset();
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
        this.unlockedCheckpoints.add(checkpointId);
    }

    public boolean hasCompletedCheckpoint(int checkpointId) {
        return this.completedCheckpoints.contains(checkpointId);
    }

    public void unlockCheckpoint(int checkpointId) {
        if (checkpointId >= 1) {
            this.unlockedCheckpoints.add(checkpointId);
        }
    }

    public boolean isCheckpointUnlocked(int checkpointId) {
        return this.unlockedCheckpoints.contains(checkpointId)
                || this.completedCheckpoints.contains(checkpointId);
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

    public Set<Integer> getUnlockedCheckpoints() {
        return Collections.unmodifiableSet(unlockedCheckpoints);
    }

    public ClueArchive getClueArchive() {
        return clueArchive;
    }

    public List<Clue> getDiscoveredMainClues() {
        return clueArchive.getDiscoveredMainClues();
    }

    public void restoreState(
            int score,
            int hintsUsed,
            Collection<Integer> completed,
            Collection<Integer> unlocked,
            Collection<String> clueIds
    ) {
        this.score = Math.max(0, score);
        this.hintsUsed = Math.max(0, hintsUsed);

        this.completedCheckpoints.clear();
        if (completed != null) {
            for (Integer cp : completed) {
                if (cp != null && cp >= 1) {
                    this.completedCheckpoints.add(cp);
                }
            }
        }

        this.unlockedCheckpoints.clear();
        this.unlockedCheckpoints.add(1);
        if (unlocked != null) {
            for (Integer cp : unlocked) {
                if (cp != null && cp >= 1) {
                    this.unlockedCheckpoints.add(cp);
                }
            }
        }
        this.unlockedCheckpoints.addAll(this.completedCheckpoints);

        this.clueArchive.restoreClueIds(clueIds);
    }
}
