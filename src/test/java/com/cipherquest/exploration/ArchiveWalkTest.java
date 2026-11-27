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
    void vaultShortcutUsesTheSameUnlockAsTheVaultStation() {
        ArchiveMap map = ArchiveMap.load();
        assertEquals('H', map.tileAt(20, 10));
        assertFalse(map.isWalkable(20, 10, checkpoint -> checkpoint <= 6));
        assertTrue(map.isWalkable(20, 10, checkpoint -> checkpoint <= 7));
        assertTrue(map.fieldNotes().stream().allMatch(note -> !note.usage().isBlank()));
    }

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
        assertRecordDesk(4, List.of(3, 5, 1), List.of(5, 3, 1));
    }

    @Test
    void receiverRequiresBothFrequencyAndPhase() {
        var signal = activityAt(3);
        assertFalse(signal.solvedBy(new FieldActivity.Attempt.Tune(4, 0)));
        assertFalse(signal.solvedBy(new FieldActivity.Attempt.Tune(3, 3)));
        assertTrue(signal.solvedBy(new FieldActivity.Attempt.Tune(4, 3)));
    }

    @Test
    void circuitRequiresAContinuousPathAndAnEastFacingOutput() {
        var circuit = (FieldActivity.Circuit) activityAt(6);
        assertFalse(circuit.solvedBy(new FieldActivity.Attempt.Wire(java.util.Collections.nCopies(16, 0))));
        var turns = new java.util.ArrayList<>(List.of(0, 3, 2, 0, 1, 2, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0));
        assertTrue(circuit.solvedBy(new FieldActivity.Attempt.Wire(turns)));
        turns.set(11, 1);
        assertFalse(circuit.solvedBy(new FieldActivity.Attempt.Wire(turns)));
    }

    @Test
    void powerPanelStartsOnTheMapAndAwardsEvidenceOnlyWhenAllDevicesAreConnected() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        FieldNote panel = panelAt(walk, 2);
        assertFalse(walk.startWorkshop(2, panel));
        walkTo(walk, 1, 0, 5);
        walkTo(walk, 0, 1, 5);
        assertTrue(walk.startWorkshop(2, panel));
        walkTo(walk, 0, -1, 3);
        assertFalse(walk.useWorkshopSwitch());
        assertEquals(RoomWorkshop.Signal.WRONG_ORDER, walk.workshop().orElseThrow().signal());
        assertTrue(walk.updateWorkshop(1).isEmpty());
        walkTo(walk, 0, -1, 1);
        walkTo(walk, -1, 0, 6);
        assertTrue(walk.useWorkshopSwitch());
        walkTo(walk, 1, 0, 6);
        walkTo(walk, 0, 1, 1);
        assertTrue(walk.useWorkshopSwitch());
        walkTo(walk, 0, 1, 2);
        walkTo(walk, -1, 0, 4);
        walkTo(walk, 0, 1, 1);
        assertTrue(walk.useWorkshopSwitch());
        assertEquals(panel, walk.updateWorkshop(0).orElseThrow());
        assertTrue(walk.workshop().isEmpty());
        assertTrue(walk.hasRecoveredClue(2));
    }

    @Test
    void freightMovesWhenThePlayerPushesAndCanBeReset() {
        var walk = new ArchiveWalk(ArchiveMap.load());
        walkTo(walk, 1, 0, 8);
        walkTo(walk, 0, 1, 5);
        assertTrue(walk.startWorkshop(5, panelAt(walk, 5)));
        walkTo(walk, 0, -1, 2);
        walkTo(walk, 1, 0, 1);
        walkTo(walk, 0, -1, 1);
        walkTo(walk, 1, 0, 1);
        var cargo = walk.workshop().orElseThrow();
        assertTrue(cargo.markers().stream().anyMatch(marker -> marker.label().equals("B")
                && marker.tile().equals(new RoomWorkshop.Tile(14, 9))));
        assertFalse(cargo.push(new RoomWorkshop.Tile(14, 9), 1, 1, tile -> true));
        assertFalse(cargo.push(new RoomWorkshop.Tile(14, 9), 1, 0, tile -> false));
        walk.resetWorkshop();
        assertEquals(new ArchiveMap.Position(11.5, 12.5), walk.position());
        assertEquals(0, walk.workshop().orElseThrow().completed());
        walk.leaveWorkshop();
        assertFalse(walk.hasRecoveredClue(5));
        FieldNote dock = panelAt(walk, 5);
        assertTrue(walk.startWorkshop(5, dock));
        walkTo(walk, 0, -1, 4);
        walkTo(walk, 1, 0, 3);
        walkTo(walk, 0, 1, 1);
        walkTo(walk, -1, 0, 1);
        walkTo(walk, 1, 0, 1);
        walkTo(walk, 0, 1, 2);
        walkTo(walk, -1, 0, 2);
        walkTo(walk, 0, -1, 2);
        assertEquals(1, walk.workshop().orElseThrow().completed());

        walkTo(walk, 1, 0, 2);
        walkTo(walk, 0, 1, 2);
        walkTo(walk, 1, 0, 2);
        walkTo(walk, 0, -1, 2);
        assertEquals(2, walk.workshop().orElseThrow().completed());

        walkTo(walk, 1, 0, 1);
        walkTo(walk, 0, -1, 1);
        walkTo(walk, 1, 0, 1);
        walkTo(walk, 0, 1, 2);
        walkTo(walk, 0, -1, 1);
        walkTo(walk, -1, 0, 2);
        walkTo(walk, 0, 1, 2);
        walkTo(walk, 1, 0, 2);
        assertEquals(dock, walk.updateWorkshop(0).orElseThrow());
        assertTrue(walk.hasRecoveredClue(5));
    }

    @Test
    void scannerOnlyClearsCheckpointSequenceWhenThePlayerEntersTheSweep() {
        var workshop = ((FieldActivity.Workshop) activityAt(7)).layout().begin();
        assertTrue(workshop.use(new RoomWorkshop.Tile(22, 6)));
        workshop.tick(1.5, new RoomWorkshop.Tile(24, 6));
        assertEquals(1, workshop.completed());
        workshop.tick(0, new RoomWorkshop.Tile(22, 6));
        assertEquals(RoomWorkshop.Signal.SCANNER_RESET, workshop.signal());
        assertEquals(0, workshop.completed());
        assertFalse(workshop.use(new RoomWorkshop.Tile(23, 9)));
        assertTrue(workshop.use(new RoomWorkshop.Tile(22, 6)));
        assertTrue(workshop.use(new RoomWorkshop.Tile(23, 9)));
        assertTrue(workshop.use(new RoomWorkshop.Tile(24, 4)));
        assertTrue(workshop.solved());
    }

    private static FieldNote panelAt(ArchiveWalk walk, int checkpoint) {
        return walk.uncollectedNotes(checkpoint).stream()
                .filter(note -> note.activity() instanceof FieldActivity.Workshop).findFirst().orElseThrow();
    }

    private static void walkTo(ArchiveWalk walk, int horizontal, int vertical, int tiles) {
        for (int slices = 0; slices < tiles * 5; slices++) {
            walk.move(horizontal, vertical, 1.0 / 22.0, gate -> true);
        }
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
