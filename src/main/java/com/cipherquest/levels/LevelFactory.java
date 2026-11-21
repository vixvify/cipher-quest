package com.cipherquest.levels;

public interface LevelFactory {

    Level createLevel(int checkpointId);

    int getTotalCheckpoints();
}
