package com.cipherquest.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("CyberAgent");
    }

    @Test
    void testInitialState() {
        assertEquals("CyberAgent", player.getUsername());
        assertEquals(0, player.getScore());
        assertEquals(0, player.getHintsUsed());
        assertTrue(player.getCompletedCheckpoints().isEmpty());
        assertTrue(player.isCheckpointUnlocked(1));
        assertFalse(player.isCheckpointUnlocked(2));
        assertNotNull(player.getClueArchive());
    }

    @Test
    void testScoreAddition() {
        player.addScore(100);
        assertEquals(100, player.getScore());

        player.addScore(50);
        assertEquals(150, player.getScore());

        player.addScore(-20);
        assertEquals(150, player.getScore());
    }

    @Test
    void testRecordHintUsed() {
        player.recordHintUsed();
        player.recordHintUsed();
        assertEquals(2, player.getHintsUsed());
    }

    @Test
    void testCompletedCheckpoints() {
        player.markCheckpointCompleted(1);
        player.markCheckpointCompleted(2);

        assertTrue(player.hasCompletedCheckpoint(1));
        assertTrue(player.hasCompletedCheckpoint(2));
        assertFalse(player.hasCompletedCheckpoint(3));
        assertEquals(2, player.getCompletedCheckpoints().size());

        assertThrows(UnsupportedOperationException.class, () -> {
            player.getCompletedCheckpoints().add(3);
        });
    }

    @Test
    void testDefaultUsername() {
        Player emptyPlayer = new Player("   ");
        assertEquals("Agent", emptyPlayer.getUsername());

        Player nullPlayer = new Player(null);
        assertEquals("Agent", nullPlayer.getUsername());
    }

    @Test
    void testUnlockCheckpointsAndRestoreState() {
        player.unlockCheckpoint(2);
        assertTrue(player.isCheckpointUnlocked(2));
        assertFalse(player.isCheckpointUnlocked(3));

        player.restoreState(
                450,
                2,
                Set.of(1, 2, 3),
                Set.of(1, 2, 3, 4),
                List.of("MC01", "MC02")
        );

        assertEquals(450, player.getScore());
        assertEquals(2, player.getHintsUsed());
        assertTrue(player.hasCompletedCheckpoint(3));
        assertTrue(player.isCheckpointUnlocked(4));
        assertTrue(player.getClueArchive().hasDiscoveredClue("MC01"));
        assertTrue(player.getClueArchive().hasDiscoveredClue("MC02"));
    }
}
