package com.cipherquest.exploration;

import com.cipherquest.util.ResourceLoader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
        return switch (properties.getProperty(id + ".activity", "source")) {
            case "source" -> new FieldActivity.Source();
            case "desk" -> new FieldActivity.RecordDesk(properties.getProperty(id + ".task"),
                    split(properties, id + ".rows"),
                    Arrays.stream(properties.getProperty(id + ".target").split(",")).map(Integer::parseInt).toList(),
                    Boolean.parseBoolean(properties.getProperty(id + ".ordered")));
            case "route" -> new FieldActivity.SurveyRoute(properties.getProperty(id + ".task"),
                    split(properties, id + ".grid"), names(properties, id + ".names"),
                    markers(properties, id + ".stops"), Set.copyOf(markers(properties, id + ".restricted")),
                    Integer.parseInt(properties.getProperty(id + ".limit")));
            default -> throw new IllegalArgumentException("Unknown field activity: " + id);
        };
    }

    private static List<String> split(Properties properties, String key) {
        return List.of(properties.getProperty(key).split("\\|"));
    }

    private static Map<Character, String> names(Properties properties, String key) {
        Map<Character, String> names = new HashMap<>();
        for (String entry : split(properties, key)) {
            names.put(entry.charAt(0), entry.substring(2));
        }
        return names;
    }

    private static List<Character> markers(Properties properties, String key) {
        return properties.getProperty(key).chars().mapToObj(marker -> (char) marker).toList();
    }
}
