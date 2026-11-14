package com.cipherquest.clues;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClueArchiveTest {

    private ClueArchive archive;

    @BeforeEach
    void setUp() {
        archive = new ClueArchive();
    }

    @Test
    void testMainClueIdempotencyAndNonSqlCheckpoints() {
        assertTrue(archive.unlockMainClueForCheckpoint(1).isEmpty());
        assertTrue(archive.unlockMainClueForCheckpoint(2).isEmpty());
        assertTrue(archive.unlockMainClueForCheckpoint(99).isEmpty());

        Optional<Clue> first = archive.unlockMainClueForCheckpoint(3);
        assertTrue(first.isPresent());
        assertEquals("MC01", first.get().getId());

        Optional<Clue> duplicate = archive.unlockMainClueForCheckpoint(3);
        assertTrue(duplicate.isEmpty());
        assertEquals(1, archive.getDiscoveredMainClues().size());
    }

    @Test
    void testRestoreClueIdsIgnoresUnknownIdsAndResetClearsArchive() {
        archive.restoreClueIds(List.of("MC01", "MC02", "NON_EXISTENT"));
        assertTrue(archive.hasDiscoveredClue("MC01"));
        assertTrue(archive.hasDiscoveredClue("MC02"));
        assertEquals(2, archive.getDiscoveredMainClues().size());
        assertEquals(2, archive.getAllDiscoveredClues().size());

        archive.reset();
        assertTrue(archive.getAllDiscoveredClues().isEmpty());
    }
}
