package com.cipherquest.core;

import com.cipherquest.levels.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for GameEngine (Core Logic)")
class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = GameEngine.getInstance();
        engine.startNewGame("TestAgent");
    }

    @Test
    @DisplayName("Starting new game initializes player, checkpoint 1, and PLAYING state")
    void testStartNewGame() {
        assertNotNull(engine.getPlayer());
        assertEquals("TestAgent", engine.getPlayer().getUsername());
        assertEquals(1, engine.getCurrentCheckpoint());
        assertEquals(GameState.PLAYING, engine.getGameState());
        assertNotNull(engine.getCurrentLevel());
        assertEquals(1, engine.getCurrentLevel().getCheckpointId());
        assertEquals(Level.ScreenType.PASSKEY, engine.getCurrentLevel().getScreenType());
    }

    @Test
    @DisplayName("Submitting wrong answer reduces lives and keeps state PLAYING")
    void testSubmitWrongAnswer() {
        AnswerResult result = engine.submitAnswer("WRONG_ANSWER");

        assertFalse(result.isCorrect());
        assertEquals(AnswerResult.Status.WRONG, result.getStatus());
        assertEquals(2, engine.getPlayer().getLives());
        assertEquals(GameState.PLAYING, engine.getGameState());
    }

    @Test
    @DisplayName("Losing all lives transitions game state to GAME_OVER")
    void testGameOverOnZeroLives() {
        engine.submitAnswer("WRONG_1"); // lives = 2
        engine.submitAnswer("WRONG_2"); // lives = 1
        AnswerResult result = engine.submitAnswer("WRONG_3"); // lives = 0

        assertFalse(result.isCorrect());
        assertEquals(AnswerResult.Status.GAME_OVER, result.getStatus());
        assertEquals(0, engine.getPlayer().getLives());
        assertEquals(GameState.GAME_OVER, engine.getGameState());

        // Further submissions should result in ERROR
        AnswerResult afterOver = engine.submitAnswer("ANYTHING");
        assertEquals(AnswerResult.Status.ERROR, afterOver.getStatus());
    }

    @Test
    @DisplayName("Submitting correct answer in Level 1 clears checkpoint and awards score")
    void testSubmitCorrectAnswer() {
        // CaesarLevel expected answer is "CIPHERQUEST"
        AnswerResult result = engine.submitAnswer("CIPHERQUEST");

        assertTrue(result.isCorrect());
        assertEquals(AnswerResult.Status.CORRECT, result.getStatus());
        assertTrue(result.getPointsAwarded() > 0);
        assertTrue(engine.getPlayer().getScore() > 0);
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());
        assertTrue(engine.getCurrentLevel().isCompleted());
    }

    @Test
    @DisplayName("Advancing checkpoint switches to Level 2 (Terminal) and updates state to PLAYING")
    void testAdvanceToNextCheckpoint() {
        engine.submitAnswer("CIPHERQUEST");
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());

        boolean advanced = engine.advanceToNextCheckpoint();
        assertTrue(advanced);
        assertEquals(2, engine.getCurrentCheckpoint());
        assertEquals(GameState.PLAYING, engine.getGameState());
        assertEquals(Level.ScreenType.TERMINAL, engine.getCurrentLevel().getScreenType());
    }

    @Test
    @DisplayName("Requesting hint marks hint as used and records in player")
    void testRequestHint() {
        assertEquals(0, engine.getPlayer().getHintsUsed());
        assertFalse(engine.getCurrentLevel().hasUsedHint());

        String hint = engine.requestHint();
        assertNotNull(hint);
        assertTrue(engine.getCurrentLevel().hasUsedHint());
        assertEquals(1, engine.getPlayer().getHintsUsed());
    }

    @Test
    @DisplayName("GameEventListener receives events when answers are submitted")
    void testEventListener() {
        final boolean[] received = {false};
        GameEventListener listener = new GameEventListener() {
            @Override
            public void onAnswerEvaluated(AnswerResult result) {
                received[0] = true;
            }
        };

        engine.addListener(listener);
        engine.submitAnswer("ANY_INPUT");
        assertTrue(received[0]);
        engine.removeListener(listener);
    }
}
