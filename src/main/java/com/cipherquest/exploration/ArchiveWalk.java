package com.cipherquest.exploration;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;

public final class ArchiveWalk {
    private static final double SPEED = 4.4;
    private static final double RADIUS = 0.23;
    private static final double REACH = 1.12;

    private final ArchiveMap map;
    private final Set<String> collectedNotes = new HashSet<>();
    private double x;
    private double y;
    private double distance;
    private Facing facing = Facing.SOUTH;

    public ArchiveWalk(ArchiveMap map) {
        this.map = map;
        x = map.spawn().x();
        y = map.spawn().y();
    }

    public ArchiveMap map() {
        return map;
    }

    public ArchiveMap.Position position() {
        return new ArchiveMap.Position(x, y);
    }

    public Facing facing() {
        return facing;
    }

    public double distanceWalked() {
        return distance;
    }

    public void move(int horizontal, int vertical, double seconds, IntPredicate isUnlocked) {
        double directionLength = Math.hypot(horizontal, vertical);
        if (directionLength == 0 || seconds <= 0) {
            return;
        }
        facing = horizontal == 0 ? (vertical < 0 ? Facing.NORTH : Facing.SOUTH)
                : (horizontal < 0 ? Facing.WEST : Facing.EAST);
        double step = SPEED * Math.min(seconds, 0.05) / directionLength;
        double beforeX = x;
        double beforeY = y;
        if (clearAt(x + horizontal * step, y, isUnlocked)) {
            x += horizontal * step;
        }
        if (clearAt(x, y + vertical * step, isUnlocked)) {
            y += vertical * step;
        }
        distance += Math.hypot(x - beforeX, y - beforeY);
    }

    public Optional<ArchiveMap.Station> nearbyStation() {
        ArchiveMap.Station closest = null;
        double nearest = REACH;
        for (ArchiveMap.Station station : map.stations()) {
            double range = Math.hypot(x - station.column() - 0.5, y - station.row() - 0.5);
            if (range < nearest) {
                closest = station;
                nearest = range;
            }
        }
        return Optional.ofNullable(closest);
    }

    public boolean hasRecoveredClue(int checkpoint) {
        return collectedNotes(checkpoint).stream().anyMatch(note -> !(note.activity() instanceof FieldActivity.Source));
    }

    public int collectedEvidenceCount() {
        return collectedNotes.size();
    }

    public List<FieldNote> uncollectedNotes(int checkpoint) {
        return notes(checkpoint, false);
    }

    public List<FieldNote> collectedNotes(int checkpoint) {
        return notes(checkpoint, true);
    }

    public Optional<FieldNote> nearbyNote(int checkpoint) {
        return uncollectedNotes(checkpoint).stream()
                .filter(note -> distanceTo(note.position()) < REACH)
                .min(Comparator.comparingDouble(note -> distanceTo(note.position())));
    }

    public Optional<FieldNote> completeNote(int checkpoint, FieldNote target, FieldActivity.Attempt attempt) {
        Optional<FieldNote> note = nearbyNote(checkpoint)
                .filter(target::equals)
                .filter(nearby -> nearby.activity().solvedBy(attempt));
        note.ifPresent(found -> collectedNotes.add(found.id()));
        return note;
    }

    private List<FieldNote> notes(int checkpoint, boolean collected) {
        return map.fieldNotes().stream()
                .filter(note -> note.checkpoint() == checkpoint && collectedNotes.contains(note.id()) == collected)
                .toList();
    }

    private double distanceTo(ArchiveMap.Position target) {
        return Math.hypot(x - target.x(), y - target.y());
    }

    private boolean clearAt(double nextX, double nextY, IntPredicate isUnlocked) {
        int left = (int) Math.floor(nextX - RADIUS);
        int right = (int) Math.floor(nextX + RADIUS);
        int top = (int) Math.floor(nextY - RADIUS);
        int bottom = (int) Math.floor(nextY + RADIUS);
        return map.isWalkable(left, top, isUnlocked) && map.isWalkable(right, top, isUnlocked)
                && map.isWalkable(left, bottom, isUnlocked) && map.isWalkable(right, bottom, isUnlocked);
    }

    public enum Facing { NORTH, SOUTH, WEST, EAST }
}
