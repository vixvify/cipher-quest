package com.cipherquest.core;

import com.cipherquest.levels.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = GameEngine.getInstance();
        engine.startNewGame("TestAgent");
    }

    @Test
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
    void testSubmitWrongAnswer() {
        AnswerResult result = engine.submitAnswer("WRONG_ANSWER");

        assertFalse(result.isCorrect());
        assertEquals(AnswerResult.Status.WRONG, result.getStatus());
        assertEquals(GameState.PLAYING, engine.getGameState());
    }

    @Test
    void testSubmitCorrectAnswer() {
        AnswerResult result = engine.submitAnswer("CIPHERQUEST");

        assertTrue(result.isCorrect());
        assertEquals(AnswerResult.Status.CORRECT, result.getStatus());
        assertTrue(result.getPointsAwarded() > 0);
        assertTrue(engine.getPlayer().getScore() > 0);
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());
        assertTrue(engine.getCurrentLevel().isCompleted());
    }

    @Test
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
    void testRequestHint() {
        assertEquals(0, engine.getPlayer().getHintsUsed());
        assertFalse(engine.getCurrentLevel().hasUsedHint());

        String hint = engine.requestHint();
        assertNotNull(hint);
        assertTrue(engine.getCurrentLevel().hasUsedHint());
        assertEquals(1, engine.getPlayer().getHintsUsed());
    }

    @Test
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
