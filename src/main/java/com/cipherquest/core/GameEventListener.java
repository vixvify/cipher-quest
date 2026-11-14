package com.cipherquest.core;

import com.cipherquest.levels.Level;
import com.cipherquest.player.Player;

public interface GameEventListener {

    default void onGameStateChanged(GameState oldState, GameState newState) {}

    default void onLevelLoaded(Level level) {}

    default void onAnswerEvaluated(AnswerResult result) {}

    default void onScoreChanged(int currentScore, int delta) {}

    default void onHintUsed(String hint) {}

    default void onGameCompleted(Player player) {}
}
