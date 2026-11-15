package com.cipherquest.core;

import com.cipherquest.levels.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        engine = GameEngine.getInstance();
        engine.clearListeners();
        engine.setSaveManager(new ProgressSaveManager(tempDir.resolve("savegame.properties")));
        engine.startNewGame("TestAgent");
    }

    @AfterEach
    void tearDown() {
        engine.clearListeners();
        engine.setSaveManager(new ProgressSaveManager());
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
        AnswerResult result = engine.submitAnswer("QUEST");

        assertTrue(result.isCorrect());
        assertEquals(AnswerResult.Status.CORRECT, result.getStatus());
        assertTrue(result.getPointsAwarded() > 0);
        assertTrue(engine.getPlayer().getScore() > 0);
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());
        assertTrue(engine.getCurrentLevel().isCompleted());
    }

    @Test
    void testAdvanceToNextCheckpoint() {
        engine.submitAnswer("QUEST");
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());

        boolean advanced = engine.advanceToNextCheckpoint();
        assertTrue(advanced);
        assertEquals(2, engine.getCurrentCheckpoint());
        assertEquals(GameState.PLAYING, engine.getGameState());
        assertEquals(Level.ScreenType.TERMINAL, engine.getCurrentLevel().getScreenType());
    }

    @Test
    void testLinuxTerminalLevelInteraction() {
        engine.submitAnswer("QUEST");
        engine.advanceToNextCheckpoint();

        assertEquals(2, engine.getCurrentCheckpoint());

        AnswerResult pwdResult = engine.submitAnswer("pwd");
        assertFalse(pwdResult.isCorrect());
        assertEquals("/home/agent", pwdResult.getMessage());

        AnswerResult lsResult = engine.submitAnswer("ls");
        assertFalse(lsResult.isCorrect());
        assertTrue(lsResult.getMessage().contains("readme.txt"));

        AnswerResult catResult = engine.submitAnswer("cat readme.txt");
        assertFalse(catResult.isCorrect());
        assertTrue(catResult.getMessage().contains("/opt/security/"));

        AnswerResult cdResult = engine.submitAnswer("cd /opt/security");
        assertFalse(cdResult.isCorrect());

        AnswerResult execResult = engine.submitAnswer("./breach.bin");
        assertTrue(execResult.isCorrect());
        assertEquals(GameState.CHECKPOINT_CLEARED, engine.getGameState());
        assertTrue(execResult.getMessage().contains("ROOT ACCESS ACQUIRED"));
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

    @Test
    void testFullSevenCheckpointPlaythroughToVictory() {
        completeCheckpointsThrough(7);

        assertEquals(GameState.VICTORY, engine.getGameState());
        assertTrue(engine.isMainEndingUnlocked());
        assertEquals(5, engine.getPlayer().getDiscoveredMainClues().size());
        assertTrue(engine.getPlayer().getClueArchive().hasDiscoveredClue("MC01"));
        assertTrue(engine.getPlayer().getClueArchive().hasDiscoveredClue("MC05"));
    }

    @Test
    void testSequentialUnlockingAndFreeReplayWithoutLosingProgress() {
        assertTrue(engine.isCheckpointUnlocked(1));
        assertFalse(engine.isCheckpointUnlocked(2));
        assertFalse(engine.isCheckpointUnlocked(3));
        assertFalse(engine.selectCheckpoint(3), "Locked mission must not be selectable");

        completeCheckpointsThrough(4);
        assertTrue(engine.isCheckpointUnlocked(5));
        int scoreBeforeReplay = engine.getPlayer().getScore();
        int mainClueCountBeforeReplay = engine.getPlayer().getDiscoveredMainClues().size();

        assertTrue(engine.selectCheckpoint(3), "Completed mission must be replayable");
        assertEquals(3, engine.getCurrentCheckpoint());
        assertEquals(GameState.PLAYING, engine.getGameState());

        AnswerResult replayResult = engine.submitAnswer(
                "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'"
        );
        assertTrue(replayResult.isCorrect());
        assertEquals(0, replayResult.getPointsAwarded());
        assertEquals(scoreBeforeReplay, engine.getPlayer().getScore());
        assertEquals(mainClueCountBeforeReplay, engine.getPlayer().getDiscoveredMainClues().size());
        assertTrue(engine.isCheckpointUnlocked(5), "Replaying earlier mission must preserve unlocked missions");
        assertTrue(engine.getPlayer().hasCompletedCheckpoint(4));
    }

    @Test
    void testThreeLevelProgressiveHintsOnSqlMissions() {
        completeCheckpointsThrough(2);
        engine.advanceToNextCheckpoint();
        assertEquals(3, engine.getCurrentCheckpoint());

        String hint1 = engine.requestHint();
        String hint2 = engine.requestHint();
        String hint3 = engine.requestHint();

        assertTrue(hint1.contains("ระดับ 1"));
        assertTrue(hint2.contains("ระดับ 2"));
        assertTrue(hint3.contains("ระดับ 3"));
        assertEquals(3, engine.getCurrentLevel().getHintLevel());
        assertEquals(1, engine.getPlayer().getHintsUsed());
    }

    @Test
    void testProgressiveHintsAndClampingAcrossAllSqlMissions() {
        completeCheckpointsThrough(2);
        int expectedHintsUsed = 0;

        for (int cp = 3; cp <= 7; cp++) {
            assertTrue(engine.advanceToNextCheckpoint());
            assertEquals(cp, engine.getCurrentCheckpoint());
            assertEquals(3, engine.getCurrentLevel().getMaxHintLevel());

            String hint1 = engine.requestHint();
            expectedHintsUsed++;
            assertTrue(hint1.contains("ระดับ 1"), "CP " + cp + " hint 1 should be ระดับ 1");
            assertEquals(1, engine.getCurrentLevel().getHintLevel());
            assertEquals(expectedHintsUsed, engine.getPlayer().getHintsUsed());

            String hint2 = engine.requestHint();
            assertTrue(hint2.contains("ระดับ 2"), "CP " + cp + " hint 2 should be ระดับ 2");
            assertEquals(2, engine.getCurrentLevel().getHintLevel());
            assertEquals(expectedHintsUsed, engine.getPlayer().getHintsUsed());

            String hint3 = engine.requestHint();
            assertTrue(hint3.contains("ระดับ 3"), "CP " + cp + " hint 3 should be ระดับ 3");
            assertEquals(3, engine.getCurrentLevel().getHintLevel());

            String hint4 = engine.requestHint();
            assertEquals(hint3, hint4, "CP " + cp + " hint beyond Level 3 should clamp");
            assertEquals(3, engine.getCurrentLevel().getHintLevel());
            assertEquals(expectedHintsUsed, engine.getPlayer().getHintsUsed());

            AnswerResult result = switch (cp) {
                case 3 -> submitM01();
                case 4 -> submitM02();
                case 5 -> submitM03();
                case 6 -> submitM04();
                case 7 -> submitM05();
                default -> throw new IllegalStateException("Unexpected checkpoint: " + cp);
            };
            assertTrue(result.isCorrect(), "Expected CP " + cp + " solution to succeed");
        }

        assertEquals(5, engine.getPlayer().getHintsUsed());
    }

    @Test
    void testListenerCanRemoveItselfDuringCallback() {
        final int[] secondListenerCalls = {0};
        GameEventListener selfRemoving = new GameEventListener() {
            @Override
            public void onAnswerEvaluated(AnswerResult result) {
                engine.removeListener(this);
            }
        };
        GameEventListener steadyListener = new GameEventListener() {
            @Override
            public void onAnswerEvaluated(AnswerResult result) {
                secondListenerCalls[0]++;
            }
        };

        engine.addListener(selfRemoving);
        engine.addListener(steadyListener);

        assertDoesNotThrow(() -> engine.submitAnswer("WRONG"));
        assertDoesNotThrow(() -> engine.submitAnswer("WRONG_AGAIN"));
        assertEquals(2, secondListenerCalls[0]);
    }

    @Test
    void testDatabaseLevelFeedbackDistinguishesErrorEmptyAndWrong() {
        completeCheckpointsThrough(2);
        engine.advanceToNextCheckpoint();
        assertEquals(3, engine.getCurrentCheckpoint());

        AnswerResult syntaxErr = engine.submitAnswer("SELECT * FORM explorers");
        assertFalse(syntaxErr.isCorrect());
        assertTrue(syntaxErr.getMessage().startsWith("ข้อผิดพลาด SQL"));

        AnswerResult emptyRes = engine.submitAnswer(
                "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'UnknownPerson'"
        );
        assertFalse(emptyRes.isCorrect());
        assertTrue(emptyRes.getMessage().startsWith("ไม่พบข้อมูล"));

        AnswerResult wrongRes = engine.submitAnswer(
                "SELECT explorer_id, explorer_name, status, clue FROM explorers"
        );
        assertFalse(wrongRes.isCorrect());
        assertTrue(wrongRes.getMessage().startsWith("ผลลัพธ์ยังไม่ถูกต้อง"));
    }

    @Test
    void testSaveAndLoadProgressPersistence() {
        completeCheckpointsThrough(4);
        engine.advanceToNextCheckpoint();
        int savedScore = engine.getPlayer().getScore();

        assertTrue(engine.saveProgress());
        assertTrue(engine.getSaveManager().hasSavedGame());

        engine.startNewGame("TemporaryAgent");
        assertEquals(1, engine.getCurrentCheckpoint());
        assertEquals(0, engine.getPlayer().getScore());

        assertTrue(engine.loadSavedGame());
        assertEquals("TestAgent", engine.getPlayer().getUsername());
        assertEquals(savedScore, engine.getPlayer().getScore());
        assertEquals(5, engine.getCurrentCheckpoint());
        assertTrue(engine.getPlayer().hasCompletedCheckpoint(4));
        assertTrue(engine.isCheckpointUnlocked(5));
        assertTrue(engine.getPlayer().getClueArchive().hasDiscoveredClue("MC01"));
        assertTrue(engine.getPlayer().getClueArchive().hasDiscoveredClue("MC02"));
    }

    @Test
    void testAutoSaveUpdatesCheckpointOnAdvanceWithoutManualSave() {
        assertTrue(engine.submitAnswer("QUEST").isCorrect());
        assertTrue(engine.advanceToNextCheckpoint());
        assertEquals(2, engine.getCurrentCheckpoint());

        engine.startNewGame("OtherAgent");
        assertEquals(1, engine.getCurrentCheckpoint());

        assertTrue(engine.loadSavedGame());
        assertEquals("TestAgent", engine.getPlayer().getUsername());
        assertEquals(2, engine.getCurrentCheckpoint());
        assertTrue(engine.getPlayer().hasCompletedCheckpoint(1));
        assertTrue(engine.isCheckpointUnlocked(2));
        assertFalse(engine.getCurrentLevel().isCompleted(), "Newly entered CP2 should not be completed yet");
    }

    @Test
    void testLoadSavedGameMarksCompletedCheckpointWhenSavedBeforeAdvancing() {
        assertTrue(engine.submitAnswer("QUEST").isCorrect());
        engine.startNewGame("OtherAgent");

        assertTrue(engine.loadSavedGame());
        assertEquals(1, engine.getCurrentCheckpoint());
        assertTrue(engine.getPlayer().hasCompletedCheckpoint(1));
        assertTrue(engine.isCheckpointUnlocked(2));
        assertTrue(engine.getCurrentLevel().isCompleted());
    }

    private void completeCheckpointsThrough(int targetCheckpoint) {
        if (targetCheckpoint >= 1 && engine.getCurrentCheckpoint() == 1) {
            assertTrue(engine.submitAnswer("QUEST").isCorrect());
        }
        if (targetCheckpoint >= 2) {
            engine.advanceToNextCheckpoint();
            engine.submitAnswer("cd /opt/security");
            assertTrue(engine.submitAnswer("./breach.bin").isCorrect());
        }
        if (targetCheckpoint >= 3) {
            engine.advanceToNextCheckpoint();
            assertTrue(submitM01().isCorrect());
        }
        if (targetCheckpoint >= 4) {
            engine.advanceToNextCheckpoint();
            assertTrue(submitM02().isCorrect());
        }
        if (targetCheckpoint >= 5) {
            engine.advanceToNextCheckpoint();
            assertTrue(submitM03().isCorrect());
        }
        if (targetCheckpoint >= 6) {
            engine.advanceToNextCheckpoint();
            assertTrue(submitM04().isCorrect());
        }
        if (targetCheckpoint >= 7) {
            engine.advanceToNextCheckpoint();
            AnswerResult victory = submitM05();
            assertTrue(victory.isCorrect());
            assertEquals(AnswerResult.Status.GAME_CLEARED, victory.getStatus());
        }
    }

    private AnswerResult submitM01() {
        return engine.submitAnswer(
                "SELECT explorer_id, explorer_name, status, clue FROM explorers WHERE explorer_name = 'Arin'"
        );
    }

    private AnswerResult submitM02() {
        return engine.submitAnswer(
                "SELECT log_id, explorer_id, message, clue FROM expedition_logs WHERE explorer_id = 'E001' AND message LIKE '%symbol%'"
        );
    }

    private AnswerResult submitM03() {
        return engine.submitAnswer(
                "SELECT artifact_id, artifact_name, artifact_type, power_level, clue FROM artifacts WHERE location_id = 'L002' AND power_level BETWEEN 50 AND 100 ORDER BY power_level DESC LIMIT 5"
        );
    }

    private AnswerResult submitM04() {
        return engine.submitAnswer(
                "SELECT a.artifact_name, e.experiment_type, e.result, e.clue FROM artifacts a JOIN experiments e ON a.artifact_id = e.artifact_id WHERE a.artifact_id = 'A003'"
        );
    }

    private AnswerResult submitM05() {
        return engine.submitAnswer(
                "SELECT a.artifact_name, a.artifact_type, l.location_name, a.power_level, a.clue "
                        + "FROM artifacts a JOIN locations l ON a.location_id = l.location_id "
                        + "WHERE a.location_id = 'L005' AND a.artifact_type = 'Treasure' "
                        + "AND a.power_level = (SELECT MAX(power_level) FROM artifacts WHERE location_id = 'L005' AND artifact_type = 'Treasure')"
        );
    }

    @Test
    void testRequestHintDuringReplayDoesNotInflateHintsUsed() {
        completeCheckpointsThrough(3);
        int hintsBeforeReplay = engine.getPlayer().getHintsUsed();
        assertEquals(0, hintsBeforeReplay);

        assertTrue(engine.selectCheckpoint(3));
        assertTrue(engine.getCurrentLevel().isCompleted());
        String replayHint = engine.requestHint();
        assertTrue(replayHint.contains("ระดับ 1"));
        assertEquals(hintsBeforeReplay, engine.getPlayer().getHintsUsed());
    }

    @Test
    void testLoadSavedGameAfterVictoryAndRetryCurrentCheckpoint() {
        completeCheckpointsThrough(7);
        assertEquals(GameState.VICTORY, engine.getGameState());
        assertTrue(engine.isMainEndingUnlocked());

        engine.startNewGame("TemporaryPlayer");
        assertFalse(engine.isMainEndingUnlocked());

        assertTrue(engine.loadSavedGame());
        assertTrue(engine.isMainEndingUnlocked());
        assertEquals(7, engine.getCurrentCheckpoint());
        assertTrue(engine.getCurrentLevel().isCompleted());
        assertEquals(5, engine.getPlayer().getDiscoveredMainClues().size());

        engine.selectCheckpoint(1);
        assertFalse(engine.requestHint().isBlank());
        engine.retryCurrentCheckpoint();
        assertEquals(1, engine.getCurrentCheckpoint());
        assertEquals(GameState.PLAYING, engine.getGameState());
        assertEquals(0, engine.getCurrentLevel().getAttempts());
    }
}
