package com.cipherquest.clues;

import com.cipherquest.util.ResourceLoader;

import java.util.*;

public class ClueArchive {

    private final Map<String, Clue> catalog;
    private final Map<Integer, String> checkpointToMainClueId;
    private final Set<String> discoveredClueIds;

    public ClueArchive() {
        this.catalog = new LinkedHashMap<>();
        this.checkpointToMainClueId = new LinkedHashMap<>();
        this.discoveredClueIds = new LinkedHashSet<>();
        initializeCatalog();
    }

    private void initializeCatalog() {
        Properties properties = ResourceLoader.loadProperties("/data/clues/clues.properties");

        for (int i = 1; properties.containsKey("clue." + i + ".id"); i++) {
            String prefix = "clue." + i + ".";
            String id = properties.getProperty(prefix + "id");
            String checkpointStr = properties.getProperty(prefix + "checkpoint");
            String title = properties.getProperty(prefix + "title");
            String typeStr = properties.getProperty(prefix + "type", "MAIN");
            String source = properties.getProperty(prefix + "source");
            String summary = properties.getProperty(prefix + "summary");

            List<String> details = new ArrayList<>();
            for (int d = 1; properties.containsKey(prefix + "details." + d); d++) {
                details.add(properties.getProperty(prefix + "details." + d));
            }

            ClueType type = ClueType.valueOf(typeStr.trim().toUpperCase(Locale.ROOT));
            Clue clue = new Clue(id, title, type, source, summary, details);
            catalog.put(clue.getId(), clue);

            if (checkpointStr != null && !checkpointStr.isBlank()) {
                checkpointToMainClueId.put(Integer.parseInt(checkpointStr.trim()), id);
            }
        }
    }

    public Optional<Clue> unlockMainClueForCheckpoint(int checkpointId) {
        String clueId = checkpointToMainClueId.get(checkpointId);
        if (clueId == null) {
            return Optional.empty();
        }
        if (discoveredClueIds.add(clueId)) {
            return Optional.ofNullable(catalog.get(clueId));
        }
        return Optional.empty();
    }

    public List<Clue> getDiscoveredMainClues() {
        return getDiscoveredCluesByType(ClueType.MAIN);
    }

    public List<Clue> getAllDiscoveredClues() {
        return getDiscoveredCluesByType(null);
    }

    private List<Clue> getDiscoveredCluesByType(ClueType type) {
        List<Clue> result = new ArrayList<>();
        for (Clue clue : catalog.values()) {
            if (discoveredClueIds.contains(clue.getId()) && (type == null || clue.getType() == type)) {
                result.add(clue);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean hasDiscoveredClue(String clueId) {
        return clueId != null && discoveredClueIds.contains(clueId);
    }

    public Set<String> getDiscoveredClueIds() {
        return Collections.unmodifiableSet(discoveredClueIds);
    }

    public void restoreClueIds(Collection<String> clueIds) {
        discoveredClueIds.clear();
        if (clueIds == null) {
            return;
        }
        for (String clueId : clueIds) {
            if (clueId == null) {
                continue;
            }
            String trimmed = clueId.trim();
            if (catalog.containsKey(trimmed)) {
                discoveredClueIds.add(trimmed);
            }
        }
    }

    public void reset() {
        discoveredClueIds.clear();
    }
}
