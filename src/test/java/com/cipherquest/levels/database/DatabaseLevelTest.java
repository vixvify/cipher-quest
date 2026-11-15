package com.cipherquest.levels.database;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseLevelTest {

    @Test
    void testSqlMissionsHaveCompleteMetadataAndNoAnswerIdSpoilers() {
        List<String> spoilerIds = List.of("E001", "L002", "A003", "L005");

        for (int cp = 3; cp <= 7; cp++) {
            try (DatabaseLevel level = new DatabaseLevel(cp)) {
                level.loadChallenge();
                assertEquals("M0" + (cp - 2), level.getMissionCode());
                assertFalse(level.getRequiredTables().isEmpty());
                assertEquals("MC0" + (cp - 2), level.getMainClueId());
                assertEquals(3, level.getMaxHintLevel());
                assertFalse(level.getStory().isBlank());
                assertFalse(level.getTask().isBlank());

                for (String spoiler : spoilerIds) {
                    assertFalse(
                            level.getStory().contains(spoiler),
                            "CP " + cp + " story must not spoil " + spoiler
                    );
                    assertFalse(
                            level.getTask().contains(spoiler),
                            "CP " + cp + " task must not spoil " + spoiler
                    );
                }
            }
        }

        try (DatabaseLevel m02 = new DatabaseLevel(4)) {
            m02.loadChallenge();
            assertTrue(
                    m02.getTask().contains("symbol"),
                    "M02 task must keep the non-spoiler 'symbol' filter requirement"
            );
        }
    }

    @Test
    void testFeedbackPluralizationLazyDbReinitAndReloadReset() {
        try (DatabaseLevel m01 = new DatabaseLevel(3)) {
            m01.loadChallenge();

            assertFalse(m01.checkAnswer("SELECT explorer_id, explorer_name, status, clue FROM explorers"));
            assertTrue(m01.getFailureFeedback().contains("(4 แถว)"), m01.getFailureFeedback());

            assertTrue(m01.useHint());
            assertTrue(m01.checkAnswer(m01.getExpectedQuery()));
            assertTrue(m01.getSuccessFeedback().contains("(1 แถว)"), m01.getSuccessFeedback());
            assertNotNull(m01.getLastQueryResult());

            m01.markCompleted();
            assertTrue(m01.isCompleted());
            assertEquals(2, m01.getAttempts());
            assertTrue(m01.hasUsedHint());

            m01.close();
            assertTrue(m01.getDbEnvironment().isInitialized());

            m01.loadChallenge();
            assertFalse(m01.isCompleted());
            assertEquals(0, m01.getAttempts());
            assertEquals(0, m01.getHintLevel());
            assertFalse(m01.hasUsedHint());
            assertNull(m01.getLastQueryResult());
        }
    }
}
