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
    void evidenceRequiresProximityAndIsScopedToTheCurrentStation() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        FieldNote note = walk.uncollectedNotes(2).stream()
                .filter(item -> item.id().equals("02-procedure"))
                .findFirst().orElseThrow();
        var read = new FieldActivity.Attempt.Read();
        assertTrue(walk.completeNote(2, note, read).isEmpty());
        for (int step = 0; step < 8; step++) {
            walk.move(0, 1, 0.05, checkpoint -> checkpoint <= 2);
        }
        assertTrue(walk.completeNote(1, note, read).isEmpty());
        assertTrue(walk.completeNote(2, note, new FieldActivity.Attempt.Records(List.of(0))).isEmpty());
        assertEquals(note, walk.completeNote(2, note, read).orElseThrow());
        assertTrue(walk.completeNote(2, note, read).isEmpty());
        assertEquals(List.of(note), walk.collectedNotes(2));
        assertTrue(walk.collectedNotes(3).isEmpty());
        assertFalse(walk.hasRecoveredClue(2));
    }

    @Test
    void triangulationAndOrderedEvidenceRejectIncorrectSelections() {
        assertRecordDesk(1, List.of(3), List.of(2));
        assertRecordDesk(3, List.of(4), List.of(0));
        assertRecordDesk(4, List.of(3, 5, 1), List.of(5, 3, 1));
        assertRecordDesk(5, List.of(1, 4, 5), List.of(1, 4, 3));
        assertRecordDesk(6, List.of(6, 4), List.of(4, 3));
    }

    @Test
    void surveyRoutesRequireSafeAdjacentStepsAndVisitStopsInOrder() {
        var terminal = (FieldActivity.SurveyRoute) activityAt(2);
        assertTrue(terminal.solvedBy(new FieldActivity.Attempt.Route(List.of(0, 5, 10, 11, 12, 13, 14, 19, 24))));
        assertFalse(terminal.solvedBy(new FieldActivity.Attempt.Route(List.of(0, 5, 10, 15, 20, 21, 22, 23, 24))));
        assertFalse(terminal.solvedBy(new FieldActivity.Attempt.Route(List.of(0, 10, 14, 24))));
        assertFalse(terminal.canStep(List.of(0, 5), 0));

        var vault = (FieldActivity.SurveyRoute) activityAt(7);
        assertTrue(vault.solvedBy(new FieldActivity.Attempt.Route(List.of(0, 5, 10, 11, 12, 13, 8, 9, 14, 19, 24))));
        assertFalse(vault.solvedBy(new FieldActivity.Attempt.Route(List.of(0, 1, 2, 3, 4, 9, 8, 13, 14, 19, 24))));
        assertFalse(vault.canStep(List.of(0, 5, 10, 11, 12, 13, 8, 9, 14, 19, 24), 23));
    }

    private static void assertRecordDesk(int checkpoint, List<Integer> correct, List<Integer> incorrect) {
        FieldActivity desk = activityAt(checkpoint);
        assertTrue(desk.solvedBy(new FieldActivity.Attempt.Records(correct)));
        assertFalse(desk.solvedBy(new FieldActivity.Attempt.Records(incorrect)));
        assertFalse(desk.solvedBy(new FieldActivity.Attempt.Records(List.of())));
    }

    private static FieldActivity activityAt(int checkpoint) {
        return ArchiveMap.load().fieldNotes().stream()
                .filter(note -> note.checkpoint() == checkpoint && !(note.activity() instanceof FieldActivity.Source))
                .findFirst().orElseThrow().activity();
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
        assertEquals(IntStream.rangeClosed(1, 7).boxed().toList(),
                map.stations().stream().map(ArchiveMap.Station::checkpoint).sorted().toList());
        for (var station : map.stations()) {
            Set<Cell> reachable = reachableAt(map, station.checkpoint());
            assertTrue(neighbors(new Cell(station.column(), station.row())).stream().anyMatch(reachable::contains),
                    "No reachable approach to checkpoint " + station.checkpoint());
            List<FieldNote> notes = map.fieldNotes().stream()
                    .filter(note -> note.checkpoint() == station.checkpoint()).toList();
            assertEquals(3, notes.size());
            assertEquals(2, notes.stream().filter(note -> note.activity() instanceof FieldActivity.Source).count());
            for (FieldNote note : notes) {
                assertTrue(reachable.contains(new Cell((int) note.position().x(), (int) note.position().y())),
                        "Cannot reach field note " + note.id());
                assertTrue(map.stations().stream().allMatch(other ->
                        Math.hypot(other.column() + 0.5 - note.position().x(), other.row() + 0.5 - note.position().y()) > 2.24),
                        "Field note overlaps a station interaction: " + note.id());
                assertTrue(notes.stream().filter(other -> other != note).allMatch(other ->
                        Math.hypot(other.position().x() - note.position().x(), other.position().y() - note.position().y()) > 2.24),
                        "Field note overlaps another field interaction: " + note.id());
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
