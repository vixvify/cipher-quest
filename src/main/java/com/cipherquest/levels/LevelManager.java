package com.cipherquest.levels;

import com.cipherquest.levels.caesar.CaesarLevel;
import com.cipherquest.levels.database.DatabaseLevel;
import com.cipherquest.levels.linux.LinuxLevel;

public class LevelManager {

    private static final int TOTAL_CHECKPOINTS = 7;

    public Level createLevel(int checkpointId) {
        return switch (checkpointId) {
            case 1 -> new CaesarLevel(checkpointId);
            case 2 -> new LinuxLevel(checkpointId);
            case 3, 4, 5, 6, 7 -> new DatabaseLevel(checkpointId);
            default -> throw new IllegalArgumentException(
                    "Checkpoint id must be between 1 and " + TOTAL_CHECKPOINTS + ", got: " + checkpointId
            );
        };
    }

    public int getTotalCheckpoints() {
        return TOTAL_CHECKPOINTS;
    }
}
