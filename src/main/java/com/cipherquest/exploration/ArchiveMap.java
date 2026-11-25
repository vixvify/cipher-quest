package com.cipherquest.exploration;

import com.cipherquest.util.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

public final class ArchiveMap {
    private final List<String> rows;
    private final Position spawn;
    private final List<Station> stations;
    private final List<FieldNote> fieldNotes;

    public static ArchiveMap load() {
        return new ArchiveMap(ResourceLoader.loadString("/data/exploration/archive.map").lines().toList(), FieldNote.load());
    }

    ArchiveMap(List<String> rows) {
        this(rows, List.of());
    }

    private ArchiveMap(List<String> rows, List<FieldNote> fieldNotes) {
        this.rows = List.copyOf(rows);
        if (rows.isEmpty() || rows.getFirst().isEmpty()
                || rows.stream().anyMatch(row -> row.length() != rows.getFirst().length())) {
            throw new IllegalArgumentException("Archive map must be rectangular");
        }
        List<Station> found = new ArrayList<>();
        Position start = null;
        for (int row = 0; row < height(); row++) {
            for (int column = 0; column < width(); column++) {
                char tile = tileAt(column, row);
                if (tile == '@') {
                    if (start != null) {
                        throw new IllegalArgumentException("Archive map has more than one starting tile");
                    }
                    start = new Position(column + 0.5, row + 0.5);
                } else if (tile >= '1' && tile <= '9') {
                    found.add(new Station(tile - '0', column, row));
                }
            }
        }
        spawn = Objects.requireNonNull(start, "Archive map has no starting tile");
        stations = List.copyOf(found);
        for (FieldNote note : fieldNotes) {
            Position position = note.position();
            if (!isWalkable((int) position.x(), (int) position.y(), gate -> false)
                    || stations.stream().noneMatch(station -> station.checkpoint() == note.checkpoint())) {
                throw new IllegalArgumentException("Invalid field note placement: " + note.id());
            }
        }
        this.fieldNotes = List.copyOf(fieldNotes);
    }

    public int width() {
        return rows.getFirst().length();
    }

    public int height() {
        return rows.size();
    }

    public Position spawn() {
        return spawn;
    }

    public List<Station> stations() {
        return stations;
    }

    List<FieldNote> fieldNotes() {
        return fieldNotes;
    }

    public char tileAt(int column, int row) {
        if (column < 0 || column >= width() || row < 0 || row >= height()) {
            return '#';
        }
        return rows.get(row).charAt(column);
    }

    public static int gateCheckpoint(char tile) {
        return switch (tile) {
            case 'A' -> 3;
            case 'G' -> 7;
            default -> 0;
        };
    }

    boolean isWalkable(int column, int row, IntPredicate isUnlocked) {
        char tile = tileAt(column, row);
        return switch (tile) {
            case '.', ':', '@' -> true;
            case 'A', 'G' -> isUnlocked.test(gateCheckpoint(tile));
            default -> false;
        };
    }

    public record Position(double x, double y) {}

    public record Station(int checkpoint, int column, int row) {}
}
