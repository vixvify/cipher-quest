package com.cipherquest.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    record SurveyRoute(String task, List<String> grid, Map<Character, String> names, List<Character> stops,
                       Set<Character> restricted, int stepLimit) implements FieldActivity {
        public SurveyRoute {
            grid = List.copyOf(grid);
            names = Map.copyOf(names);
            stops = List.copyOf(stops);
            restricted = Set.copyOf(restricted);
            if (grid.isEmpty() || grid.getFirst().isEmpty() || stepLimit < 1) {
                throw new IllegalArgumentException("Invalid survey map");
            }
            int width = grid.getFirst().length();
            String cells = String.join("", grid);
            if (grid.stream().anyMatch(row -> row.length() != width)
                    || cells.chars().filter(ch -> ch == 'S').count() != 1
                    || cells.chars().filter(ch -> ch == 'E').count() != 1
                    || stops.stream().anyMatch(stop -> cells.indexOf(stop) < 0)
                    || restricted.stream().anyMatch(tile -> cells.indexOf(tile) < 0)) {
                throw new IllegalArgumentException("Invalid survey map markers");
            }
        }

        public int columns() {
            return grid.getFirst().length();
        }

        public int cellCount() {
            return grid.size() * columns();
        }

        public char markerAt(int cell) {
            return grid.get(cell / columns()).charAt(cell % columns());
        }

        public int start() {
            for (int index = 0; index < cellCount(); index++) {
                if (markerAt(index) == 'S') {
                    return index;
                }
            }
            throw new IllegalStateException("Survey route has no start");
        }

        public boolean canStep(List<Integer> path, int next) {
            if (path.isEmpty() || next < 0 || next >= cellCount() || markerAt(next) == '#'
                    || path.contains(next) || path.size() > stepLimit || markerAt(path.getLast()) == 'E') {
                return false;
            }
            int from = path.getLast();
            return Math.abs(from / columns() - next / columns()) + Math.abs(from % columns() - next % columns()) == 1;
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            if (!(attempt instanceof Attempt.Route route) || route.cells().isEmpty() || route.cells().getFirst() != start()) {
                return false;
            }
            List<Integer> path = new ArrayList<>(List.of(start()));
            List<Character> visitedStops = new ArrayList<>();
            for (int next : route.cells().subList(1, route.cells().size())) {
                if (!canStep(path, next) || restricted.contains(markerAt(next))) {
                    return false;
                }
                path.add(next);
                char marker = markerAt(next);
                if (stops.contains(marker)) {
                    visitedStops.add(marker);
                }
            }
            return markerAt(path.getLast()) == 'E' && visitedStops.equals(stops);
        }
    }

    sealed interface Attempt {
        record Read() implements Attempt {}
        record Records(List<Integer> indices) implements Attempt {
            public Records {
                indices = List.copyOf(indices);
            }
        }
        record Route(List<Integer> cells) implements Attempt {
            public Route {
                cells = List.copyOf(cells);
            }
        }
    }
}
