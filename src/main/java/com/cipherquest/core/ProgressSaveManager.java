package com.cipherquest.core;

import com.cipherquest.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class ProgressSaveManager {

    private final Path saveFilePath;

    public ProgressSaveManager() {
        this(Paths.get(System.getProperty("user.home"), ".cipherquest", "savegame.properties"));
    }

    public ProgressSaveManager(Path saveFilePath) {
        this.saveFilePath = Objects.requireNonNull(saveFilePath, "saveFilePath cannot be null");
    }

    public Path getSaveFilePath() {
        return saveFilePath;
    }

    public boolean hasSavedGame() {
        return Files.isRegularFile(saveFilePath);
    }

    public boolean saveProgress(GameEngine engine) {
        if (engine == null || engine.getPlayer() == null) {
            return false;
        }

        Player player = engine.getPlayer();
        Properties props = new Properties();
        props.setProperty("player.username", player.getUsername());
        props.setProperty("player.score", String.valueOf(player.getScore()));
        props.setProperty("player.hintsUsed", String.valueOf(player.getHintsUsed()));
        props.setProperty("progress.currentCheckpoint", String.valueOf(engine.getCurrentCheckpoint()));
        props.setProperty("progress.completedCheckpoints", formatIntSet(player.getCompletedCheckpoints()));
        props.setProperty("progress.unlockedCheckpoints", formatIntSet(player.getUnlockedCheckpoints()));
        props.setProperty("progress.discoveredClues", String.join(",", player.getClueArchive().getDiscoveredClueIds()));
        props.setProperty("progress.mainEndingUnlocked", String.valueOf(engine.isMainEndingUnlocked()));

        Path tempFile = null;
        try {
            Path parent = saveFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
                tempFile = Files.createTempFile(parent, "savegame-", ".tmp");
            } else {
                tempFile = Files.createTempFile("savegame-", ".tmp");
            }
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                props.store(out, "CipherQuest - SQL: The Lost Treasure Save Data");
            }
            try {
                Files.move(tempFile, saveFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, saveFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public Optional<SaveData> loadProgress() {
        if (!hasSavedGame()) {
            return Optional.empty();
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(saveFilePath)) {
            props.load(in);
        } catch (IOException | IllegalArgumentException e) {
            return Optional.empty();
        }

        String username = props.getProperty("player.username", "Agent");
        int score = parseIntOrDefault(props.getProperty("player.score"), 0);
        int hintsUsed = parseIntOrDefault(props.getProperty("player.hintsUsed"), 0);
        int currentCheckpoint = parseIntOrDefault(props.getProperty("progress.currentCheckpoint"), 1);
        Set<Integer> completed = parseIntSet(props.getProperty("progress.completedCheckpoints", ""));
        Set<Integer> unlocked = parseIntSet(props.getProperty("progress.unlockedCheckpoints", "1"));
        List<String> clueIds = parseStringList(props.getProperty("progress.discoveredClues", ""));
        boolean mainEndingUnlocked = Boolean.parseBoolean(props.getProperty("progress.mainEndingUnlocked", "false"));

        return Optional.of(new SaveData(
                username,
                score,
                hintsUsed,
                currentCheckpoint,
                completed,
                unlocked,
                clueIds,
                mainEndingUnlocked
        ));
    }

    public boolean deleteSavedGame() {
        try {
            return Files.deleteIfExists(saveFilePath);
        } catch (IOException e) {
            return false;
        }
    }

    private String formatIntSet(Set<Integer> values) {
        return values.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private Set<Integer> parseIntSet(String raw) {
        Set<Integer> result = new LinkedHashSet<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    result.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    private List<String> parseStringList(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private int parseIntOrDefault(String raw, int defaultValue) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static class SaveData {
        private final String username;
        private final int score;
        private final int hintsUsed;
        private final int currentCheckpoint;
        private final Set<Integer> completedCheckpoints;
        private final Set<Integer> unlockedCheckpoints;
        private final List<String> discoveredClueIds;
        private final boolean mainEndingUnlocked;

        public SaveData(
                String username,
                int score,
                int hintsUsed,
                int currentCheckpoint,
                Set<Integer> completedCheckpoints,
                Set<Integer> unlockedCheckpoints,
                List<String> discoveredClueIds,
                boolean mainEndingUnlocked
        ) {
            this.username = username != null ? username : "Agent";
            this.score = score;
            this.hintsUsed = hintsUsed;
            this.currentCheckpoint = currentCheckpoint;
            this.completedCheckpoints = completedCheckpoints != null
                    ? Collections.unmodifiableSet(new LinkedHashSet<>(completedCheckpoints))
                    : Collections.emptySet();
            this.unlockedCheckpoints = unlockedCheckpoints != null
                    ? Collections.unmodifiableSet(new LinkedHashSet<>(unlockedCheckpoints))
                    : Collections.emptySet();
            this.discoveredClueIds = discoveredClueIds != null
                    ? Collections.unmodifiableList(new ArrayList<>(discoveredClueIds))
                    : Collections.emptyList();
            this.mainEndingUnlocked = mainEndingUnlocked;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public int getHintsUsed() {
            return hintsUsed;
        }

        public int getCurrentCheckpoint() {
            return currentCheckpoint;
        }

        public Set<Integer> getCompletedCheckpoints() {
            return completedCheckpoints;
        }

        public Set<Integer> getUnlockedCheckpoints() {
            return unlockedCheckpoints;
        }

        public List<String> getDiscoveredClueIds() {
            return discoveredClueIds;
        }

        public boolean isMainEndingUnlocked() {
            return mainEndingUnlocked;
        }
    }
}
