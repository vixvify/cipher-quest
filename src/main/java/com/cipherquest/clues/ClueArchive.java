package com.cipherquest.clues;

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
        registerClue(new Clue(
                "MC01",
                "Main Clue 01 — The Missing Explorer",
                ClueType.MAIN,
                "explorers (E001)",
                "Explorer Arin (E001) is Missing; last signal detected near the Ancient Temple.",
                List.of(
                        "Explorer: Arin",
                        "Explorer ID: E001",
                        "Status: Missing",
                        "Last signal: Ancient Temple"
                )
        ));
        checkpointToMainClueId.put(3, "MC01");

        registerClue(new Clue(
                "MC02",
                "Main Clue 02 — The Last Message",
                ClueType.MAIN,
                "expedition_logs (101)",
                "Sun and Moon symbols unlock the Relic Chamber (L002).",
                List.of(
                        "Required Symbols: Sun and Moon",
                        "Destination: Relic Chamber",
                        "Location ID: L002"
                )
        ));
        checkpointToMainClueId.put(4, "MC02");

        registerClue(new Clue(
                "MC03",
                "Main Clue 03 — The Forgotten Relics",
                ClueType.MAIN,
                "artifacts (A003)",
                "Prime Core (A003) has power level 100, and its experimental records are restricted.",
                List.of(
                        "Artifact: Prime Core",
                        "Artifact ID: A003",
                        "Power Level: 100",
                        "Important Information: Experimental records are restricted"
                )
        ));
        checkpointToMainClueId.put(5, "MC03");

        registerClue(new Clue(
                "MC04",
                "Main Clue 04 — The Hidden Records",
                ClueType.MAIN,
                "experiments (303)",
                "Prime Core experiment 'Energy Amplification' (Result: Unknown) unlocks the Archive Vault at L005.",
                List.of(
                        "Artifact: Prime Core",
                        "Experiment: Energy Amplification",
                        "Result: Unknown",
                        "Destination: Archive Vault",
                        "Location ID: L005"
                )
        ));
        checkpointToMainClueId.put(6, "MC04");

        registerClue(new Clue(
                "MC05",
                "Main Clue 05 — The Lost Treasure",
                ClueType.MAIN,
                "artifacts (A008) / locations (L005)",
                "The Memory Archive in the Archive Vault (Power Level 100) contains the memories of an ancient civilization.",
                List.of(
                        "Treasure: Memory Archive",
                        "Location: Archive Vault",
                        "Power Level: 100",
                        "Description: Contains the memories of an ancient civilization"
                )
        ));
        checkpointToMainClueId.put(7, "MC05");
    }

    private void registerClue(Clue clue) {
        catalog.put(clue.getId(), clue);
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
