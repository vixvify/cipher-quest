package com.cipherquest.exploration;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveWalkTest {
    @Test
    void cipherCardCanOnlyBePickedUpWhileNearbyAndOnce() {
        ArchiveMap map = new ArchiveMap(List.of(
                "#######",
                "#@.!1.#",
                "#######"));
        var walk = new ArchiveWalk(map);
        assertFalse(walk.collectCipherCard());
        for (int step = 0; step < 5; step++) {
            walk.move(1, 0, 0.05, checkpoint -> false);
        }
        assertTrue(walk.isNearCipherCard());
        assertTrue(walk.collectCipherCard());
        assertTrue(walk.hasCipherCard());
        assertFalse(walk.collectCipherCard());
        assertFalse(new ArchiveWalk(map).hasCipherCard());
    }

    @Test
    void notesCanOnlyBePickedUpAndReadForTheirCheckpoint() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        FieldNote note = walk.uncollectedNotes(2).getFirst();
        var correctSignal = new FieldActivity.Attempt.Tune(97);
        assertTrue(walk.completeNote(2, note, correctSignal).isEmpty());
        for (int step = 0; step < 8; step++) {
            walk.move(0, 1, 0.05, checkpoint -> checkpoint <= 2);
        }
        assertTrue(walk.completeNote(1, note, correctSignal).isEmpty());
        assertTrue(walk.completeNote(2, note, new FieldActivity.Attempt.Collect()).isEmpty());
        assertTrue(walk.completeNote(2, note, new FieldActivity.Attempt.Tune(94)).isEmpty());
        assertTrue(walk.collectedNotes(2).isEmpty());
        assertEquals(note, walk.completeNote(2, note, correctSignal).orElseThrow());
        assertTrue(walk.uncollectedNotes(2).isEmpty());
        assertTrue(walk.completeNote(2, note, correctSignal).isEmpty());
        assertEquals(List.of(note), walk.collectedNotes(2));
        assertTrue(walk.collectedNotes(3).isEmpty());
    }

    @Test
    void assemblyAndMatchingRequireTheirOwnCorrectAnswer() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        var sequence = (FieldActivity.Sequence) walk.uncollectedNotes(7).stream()
                .filter(note -> note.activity() instanceof FieldActivity.Sequence).findFirst().orElseThrow().activity();
        assertFalse(sequence.solvedBy(new FieldActivity.Attempt.Assemble(List.of(0, 1, 2))));
        assertFalse(sequence.solvedBy(new FieldActivity.Attempt.Assemble(List.of(1, 0))));
        assertTrue(sequence.solvedBy(new FieldActivity.Attempt.Assemble(List.of(1, 0, 2))));

        var match = (FieldActivity.Match) walk.uncollectedNotes(6).stream()
                .filter(note -> note.activity() instanceof FieldActivity.Match).findFirst().orElseThrow().activity();
        assertFalse(match.solvedBy(new FieldActivity.Attempt.MatchMark(0)));
        assertTrue(match.solvedBy(new FieldActivity.Attempt.MatchMark(2)));
    }

    @Test
    void circuitRequiresEveryLightAndPressingOneSwitchTwiceRestoresTheBoard() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        var circuit = (FieldActivity.Circuit) walk.uncollectedNotes(4).stream()
                .filter(note -> note.activity() instanceof FieldActivity.Circuit).findFirst().orElseThrow().activity();
        assertEquals(circuit.lightsAfter(List.of()), circuit.lightsAfter(List.of(4, 4)));
        assertFalse(circuit.solvedBy(new FieldActivity.Attempt.Switches(List.of(4))));
        assertFalse(circuit.solvedBy(new FieldActivity.Attempt.Switches(List.of(4, 8, 9))));
        assertTrue(circuit.solvedBy(new FieldActivity.Attempt.Switches(List.of(4, 8))));
    }

    @Test
    void lockedGateBlocksMovementUntilItsCheckpointUnlocks() {
        ArchiveMap map = new ArchiveMap(List.of(
                "########",
                "#@.A.3.#",
                "########"));
        ArchiveWalk walk = new ArchiveWalk(map);

        for (int step = 0; step < 30; step++) {
            walk.move(1, 0, 0.05, checkpoint -> false);
        }
        assertTrue(walk.position().x() < 3);
        assertTrue(walk.nearbyStation().isEmpty());

        for (int step = 0; step < 30; step++) {
            walk.move(1, 0, 0.05, checkpoint -> checkpoint == 3);
        }
        assertTrue(walk.position().x() > 4);
        assertTrue(walk.position().x() < 5, "Movement cannot enter a console");
        assertEquals(3, walk.nearbyStation().orElseThrow().checkpoint());
    }

    @Test
    void diagonalInputPreservesWalkingSpeed() {
        ArchiveMap map = new ArchiveMap(List.of(
                "#####",
                "#...#",
                "#.@.#",
                "#...#",
                "#####"));
        var straight = new ArchiveWalk(map);
        var diagonal = new ArchiveWalk(map);
        straight.move(1, 0, 0.05, checkpoint -> false);
        diagonal.move(1, 1, 0.05, checkpoint -> false);
        assertEquals(straight.distanceWalked(), diagonal.distanceWalked(), 0.0001);
    }

    @Test
    void authoredMapHasAReachableStationForEachUnlockedCheckpoint() {
        ArchiveMap map = ArchiveMap.load();
        var card = map.cipherCard().orElseThrow();
        assertTrue(reachableAt(map, 1).contains(new Cell((int) card.x(), (int) card.y())));
        assertEquals(IntStream.rangeClosed(1, 7).boxed().toList(),
                map.stations().stream().map(ArchiveMap.Station::checkpoint).sorted().toList());
        for (var station : map.stations()) {
            Set<Cell> reachable = reachableAt(map, station.checkpoint());
            assertTrue(neighbors(new Cell(station.column(), station.row())).stream().anyMatch(reachable::contains),
                    "No reachable approach to checkpoint " + station.checkpoint());
            for (FieldNote note : map.fieldNotes().stream()
                    .filter(note -> note.checkpoint() == station.checkpoint()).toList()) {
                assertTrue(reachable.contains(new Cell((int) note.position().x(), (int) note.position().y())),
                        "Cannot reach field note " + note.id());
                assertTrue(map.stations().stream().allMatch(other ->
                        Math.hypot(other.column() + 0.5 - note.position().x(), other.row() + 0.5 - note.position().y()) > 2.24),
                        "Field note overlaps a station interaction: " + note.id());
            }
        }
    }

    private static Set<Cell> reachableAt(ArchiveMap map, int unlockedThrough) {
        var start = new Cell((int) map.spawn().x(), (int) map.spawn().y());
        Set<Cell> visited = new HashSet<>();
        var queue = new ArrayDeque<Cell>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            for (Cell cell : neighbors(queue.removeFirst())) {
                if (map.isWalkable(cell.x(), cell.y(), checkpoint -> checkpoint <= unlockedThrough)
                        && visited.add(cell)) {
                    queue.addLast(cell);
                }
            }
        }
        return visited;
    }

    private static List<Cell> neighbors(Cell cell) {
        return List.of(new Cell(cell.x() - 1, cell.y()), new Cell(cell.x() + 1, cell.y()),
                new Cell(cell.x(), cell.y() - 1), new Cell(cell.x(), cell.y() + 1));
    }

    private record Cell(int x, int y) {}
}
