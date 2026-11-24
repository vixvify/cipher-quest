package com.cipherquest.exploration;

import com.cipherquest.util.ResourceLoader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public record FieldNote(String id, int checkpoint, ArchiveMap.Position position, String title, String text, FieldActivity activity) {
    static List<FieldNote> load() {
        Properties properties = ResourceLoader.loadProperties("/data/exploration/field-notes.properties");
        return properties.stringPropertyNames().stream()
                .filter(key -> key.endsWith(".tile"))
                .map(key -> key.substring(0, key.length() - ".tile".length()))
                .map(id -> {
                    String[] tile = properties.getProperty(id + ".tile").split(",");
                    return new FieldNote(id, Integer.parseInt(properties.getProperty(id + ".checkpoint")),
                            new ArchiveMap.Position(Integer.parseInt(tile[0]) + 0.5, Integer.parseInt(tile[1]) + 0.5),
                            properties.getProperty(id + ".title"), properties.getProperty(id + ".text"),
                            loadActivity(properties, id));
                })
                .sorted(Comparator.comparing(FieldNote::id))
                .toList();
    }

    private static FieldActivity loadActivity(Properties properties, String id) {
        return switch (properties.getProperty(id + ".activity", "pickup")) {
            case "pickup" -> new FieldActivity.Pickup();
            case "radio" -> new FieldActivity.Radio(Integer.parseInt(properties.getProperty(id + ".frequency")));
            case "sequence" -> new FieldActivity.Sequence(List.of(properties.getProperty(id + ".pieces").split("\\|")),
                    readIndices(properties, id + ".order"));
            case "match" -> new FieldActivity.Match(properties.getProperty(id + ".prompt"),
                    List.of(properties.getProperty(id + ".options").split("\\|")),
                    Integer.parseInt(properties.getProperty(id + ".answer")));
            case "circuit" -> new FieldActivity.Circuit(readIndices(properties, id + ".lit"));
            default -> throw new IllegalArgumentException("Unknown field activity: " + id);
        };
    }

    private static List<Integer> readIndices(Properties properties, String key) {
        return Arrays.stream(properties.getProperty(key).split(",")).map(Integer::parseInt).toList();
    }
}
