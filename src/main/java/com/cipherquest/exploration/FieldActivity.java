package com.cipherquest.exploration;

import java.util.HashSet;
import java.util.List;

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

    sealed interface Attempt {
        record Read() implements Attempt {}
        record Records(List<Integer> indices) implements Attempt {
            public Records {
                indices = List.copyOf(indices);
            }
        }
    }
}
