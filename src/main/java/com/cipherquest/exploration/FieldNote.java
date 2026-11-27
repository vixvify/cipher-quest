package com.cipherquest.exploration;

import com.cipherquest.util.ResourceLoader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public record FieldNote(String id, int checkpoint, ArchiveMap.Position position, String title, String text,
                        String usage, FieldActivity activity) {
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
                            properties.getProperty(id + ".use", ""),
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
            case "signal" -> new FieldActivity.Signal(properties.getProperty(id + ".task"),
                    Integer.parseInt(properties.getProperty(id + ".frequency")),
                    Integer.parseInt(properties.getProperty(id + ".phase")));
            case "circuit" -> new FieldActivity.Circuit(properties.getProperty(id + ".task"),
                    Arrays.stream(properties.getProperty(id + ".tiles").split(",")).map(Integer::parseInt).toList());
            case "power", "vault" -> {
                boolean vault = properties.getProperty(id + ".activity").equals("vault");
                var kind = vault ? RoomWorkshop.Kind.VAULT : RoomWorkshop.Kind.POWER;
                var sequence = split(properties, id + ".switches").stream().map(FieldNote::device).toList();
                yield new FieldActivity.Workshop(new RoomWorkshop.Layout.Switches(kind, sequence,
                        vault ? bounds(properties.getProperty(id + ".sweep")) : null));
            }
            case "cargo" -> {
                var freight = split(properties, id + ".freight").stream().map(entry -> {
                    String[] pair = entry.split(">");
                    return new RoomWorkshop.Freight(device(pair[0]), device(pair[1]));
                }).toList();
                yield new FieldActivity.Workshop(new RoomWorkshop.Layout.LoadingDock(
                        bounds(properties.getProperty(id + ".area")), freight));
            }
            default -> throw new IllegalArgumentException("Unknown field activity: " + id);
        };
    }

    private static List<String> split(Properties properties, String key) {
        return List.of(properties.getProperty(key).split("\\|"));
    }

    private static RoomWorkshop.Device device(String value) {
        String[] parts = value.split("@");
        return new RoomWorkshop.Device(parts[0], tile(parts[1]));
    }

    private static RoomWorkshop.Tile tile(String value) {
        String[] coords = value.split(",");
        return new RoomWorkshop.Tile(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    }

    private static RoomWorkshop.Bounds bounds(String value) {
        String[] corners = value.split(":");
        return new RoomWorkshop.Bounds(tile(corners[0]), tile(corners[1]));
    }
}
