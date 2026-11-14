package com.cipherquest.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
