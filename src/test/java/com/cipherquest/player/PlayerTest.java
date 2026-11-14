package com.cipherquest.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for Player (Encapsulation)")
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("CyberAgent");
    }

    @Test
    @DisplayName("Player starts with MAX_LIVES and zero score")
    void testInitialState() {
        assertEquals("CyberAgent", player.getUsername());
        assertEquals(Player.MAX_LIVES, player.getLives());
        assertEquals(0, player.getScore());
        assertEquals(0, player.getHintsUsed());
        assertTrue(player.isAlive());
        assertTrue(player.getCompletedCheckpoints().isEmpty());
    }

    @Test
    @DisplayName("Deducting life decreases life count and detects death")
    void testDeductLife() {
        player.deductLife();
        assertEquals(2, player.getLives());
        assertTrue(player.isAlive());

        player.deductLife();
        player.deductLife();
        assertEquals(0, player.getLives());
        assertFalse(player.isAlive());

        // Lives should not drop below 0
        player.deductLife();
        assertEquals(0, player.getLives());
    }

    @Test
    @DisplayName("Adding score increases total score only with positive points")
    void testScoreAddition() {
        player.addScore(100);
        assertEquals(100, player.getScore());

        player.addScore(50);
        assertEquals(150, player.getScore());

        // Negative score should be ignored
        player.addScore(-20);
        assertEquals(150, player.getScore());
    }

    @Test
    @DisplayName("Recording hints used increments hint count")
    void testRecordHintUsed() {
        player.recordHintUsed();
        player.recordHintUsed();
        assertEquals(2, player.getHintsUsed());
    }

    @Test
    @DisplayName("Marking checkpoints updates completed checkpoints set")
    void testCompletedCheckpoints() {
        player.markCheckpointCompleted(1);
        player.markCheckpointCompleted(2);

        assertTrue(player.hasCompletedCheckpoint(1));
        assertTrue(player.hasCompletedCheckpoint(2));
        assertFalse(player.hasCompletedCheckpoint(3));
        assertEquals(2, player.getCompletedCheckpoints().size());

        // Completed set returned should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            player.getCompletedCheckpoints().add(3);
        });
    }

    @Test
    @DisplayName("Default username is assigned when null or empty")
    void testDefaultUsername() {
        Player emptyPlayer = new Player("   ");
        assertEquals("Agent", emptyPlayer.getUsername());

        Player nullPlayer = new Player(null);
        assertEquals("Agent", nullPlayer.getUsername());
    }
}

