package com.cipherquest.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProgressSaveManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testMissingAndDeleteSavedGame() {
        Path savePath = tempDir.resolve("nested").resolve("savegame.properties");
        ProgressSaveManager manager = new ProgressSaveManager(savePath);

        assertFalse(manager.hasSavedGame());
        assertTrue(manager.loadProgress().isEmpty());
        assertFalse(manager.deleteSavedGame());

        GameEngine engine = GameEngine.getInstance();
        engine.clearListeners();
        engine.setSaveManager(manager);
        engine.startNewGame("นักสืบอาริน");
        assertTrue(engine.saveProgress());
        assertTrue(manager.hasSavedGame());

        Optional<ProgressSaveManager.SaveData> loaded = manager.loadProgress();
        assertTrue(loaded.isPresent());
        assertEquals("นักสืบอาริน", loaded.get().getUsername());

        assertTrue(manager.deleteSavedGame());
        assertFalse(manager.hasSavedGame());
    }

    @Test
    void testMalformedUnicodeEscapeInPropertiesReturnsEmpty() throws Exception {
        Path savePath = tempDir.resolve("corrupted.properties");
        Files.writeString(savePath, "player.username=\\uZZZZ\n", StandardCharsets.ISO_8859_1);

        ProgressSaveManager manager = new ProgressSaveManager(savePath);
        assertTrue(manager.hasSavedGame());
        assertTrue(manager.loadProgress().isEmpty());
    }

    @Test
    void testSaveDataHandlesNullCollectionsSafely() {
        ProgressSaveManager.SaveData data = new ProgressSaveManager.SaveData(
                null, 100, 1, 2, null, null, null, false
        );
        assertEquals("Agent", data.getUsername());
        assertTrue(data.getCompletedCheckpoints().isEmpty());
        assertTrue(data.getUnlockedCheckpoints().isEmpty());
        assertTrue(data.getDiscoveredClueIds().isEmpty());
    }
}
