package com.cipherquest.exploration;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Set;

public sealed interface FieldActivity {
    boolean solvedBy(Attempt attempt);

    record Source() implements FieldActivity {
        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Read;
        }
    }

    record RecordDesk(String task, List<String> rows, List<Integer> target, boolean ordered) implements FieldActivity {
        public RecordDesk {
            rows = List.copyOf(rows);
            target = List.copyOf(target);
            int rowCount = rows.size();
            if (target.isEmpty() || new HashSet<>(target).size() != target.size()
                    || target.stream().anyMatch(index -> index < 0 || index >= rowCount)) {
                throw new IllegalArgumentException("Invalid record desk solution");
            }
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Records records && (ordered
                    ? target.equals(records.indices())
                    : records.indices().size() == target.size() && target.containsAll(records.indices())
                        && new HashSet<>(records.indices()).size() == target.size());
        }
    }

    record Workshop(RoomWorkshop.Layout layout) implements FieldActivity {
        @Override
        public boolean solvedBy(Attempt attempt) {
            return false;
        }
    }

    record Signal(String task, int frequency, int phase) implements FieldActivity {
        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Tune tune && tune.frequency() == frequency && tune.phase() == phase;
        }

        public double sample(double position, int frequency, int phase) {
            double angle = 2 * Math.PI * (position * frequency + phase / 8.0);
            return Math.sin(angle);
        }
    }

    record Circuit(String task, List<Integer> tiles) implements FieldActivity {
        private static final int SIDE = 4;
        private static final int INPUT = 4;
        private static final int OUTPUT = 11;

        public Circuit {
            tiles = List.copyOf(tiles);
            if (tiles.size() != SIDE * SIDE || tiles.stream().anyMatch(mask -> mask < 0 || mask > 15)) {
                throw new IllegalArgumentException("Invalid connector board");
            }
        }

        public int connector(int index, int turns) {
            int mask = tiles.get(index);
            int rotation = Math.floorMod(turns, 4);
            return ((mask << rotation) | (mask >>> (4 - rotation))) & 15;
        }

        public Set<Integer> powered(List<Integer> turns) {
            Set<Integer> powered = new HashSet<>();
            if (turns.size() != tiles.size() || (connector(INPUT, turns.get(INPUT)) & 8) == 0) {
                return Set.of();
            }
            var queue = new ArrayDeque<Integer>();
            powered.add(INPUT);
            queue.add(INPUT);
            int[] horizontal = {0, 1, 0, -1};
            int[] vertical = {-1, 0, 1, 0};
            while (!queue.isEmpty()) {
                int position = queue.removeFirst();
                int connected = connector(position, turns.get(position));
                for (int direction = 0; direction < 4; direction++) {
                    int x = position % SIDE + horizontal[direction];
                    int y = position / SIDE + vertical[direction];
                    if ((connected & (1 << direction)) == 0 || x < 0 || x >= SIDE || y < 0 || y >= SIDE) {
                        continue;
                    }
                    int neighbor = y * SIDE + x;
                    int reverse = 1 << ((direction + 2) % 4);
                    if ((connector(neighbor, turns.get(neighbor)) & reverse) != 0 && powered.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            return Set.copyOf(powered);
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Wire wiring && powered(wiring.turns()).contains(OUTPUT)
                    && (connector(OUTPUT, wiring.turns().get(OUTPUT)) & 2) != 0;
        }
    }

    sealed interface Attempt {
        record Read() implements Attempt {}
        record Tune(int frequency, int phase) implements Attempt {}
        record Wire(List<Integer> turns) implements Attempt {
            public Wire {
                turns = List.copyOf(turns);
            }
        }
        record Records(List<Integer> indices) implements Attempt {
            public Records {
                indices = List.copyOf(indices);
            }
        }
    }
}
