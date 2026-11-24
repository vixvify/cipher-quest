package com.cipherquest.exploration;

import java.util.List;

public sealed interface FieldActivity {
    boolean solvedBy(Attempt attempt);

    record Pickup() implements FieldActivity {
        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Collect;
        }
    }

    record Radio(int frequency) implements FieldActivity {
        public static final int MIN_FREQUENCY = 88;
        public static final int MAX_FREQUENCY = 108;

        public double signalStrength(int channel) {
            return Math.max(0.08, 1 - Math.abs(channel - frequency) / 12.0);
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Tune tune && tune.frequency() == frequency;
        }
    }

    record Sequence(List<String> pieces, List<Integer> order) implements FieldActivity {
        public Sequence {
            pieces = List.copyOf(pieces);
            order = List.copyOf(order);
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Assemble assemble && order.equals(assemble.order());
        }
    }

    record Match(String prompt, List<String> options, int answerIndex) implements FieldActivity {
        public Match {
            options = List.copyOf(options);
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.MatchMark match && answerIndex == match.index();
        }
    }

    record Circuit(List<Integer> initiallyLit) implements FieldActivity {
        public static final int SIDE = 3;
        public static final int SIZE = SIDE * SIDE;
        private static final int ALL_LIT = (1 << SIZE) - 1;

        public Circuit {
            initiallyLit = List.copyOf(initiallyLit);
        }

        public int lightsAfter(List<Integer> switches) {
            int lights = 0;
            for (int tile : initiallyLit) {
                lights |= 1 << tile;
            }
            for (int pressed : switches) {
                int row = pressed / SIDE;
                int column = pressed % SIDE;
                for (int tile = 0; tile < SIZE; tile++) {
                    int distance = Math.abs(row - tile / SIDE) + Math.abs(column - tile % SIDE);
                    if (distance <= 1) {
                        lights ^= 1 << tile;
                    }
                }
            }
            return lights;
        }

        @Override
        public boolean solvedBy(Attempt attempt) {
            return attempt instanceof Attempt.Switches switches
                    && switches.pressed().stream().allMatch(tile -> tile >= 0 && tile < SIZE)
                    && lightsAfter(switches.pressed()) == ALL_LIT;
        }
    }

    sealed interface Attempt {
        record Collect() implements Attempt {}
        record Tune(int frequency) implements Attempt {}
        record Assemble(List<Integer> order) implements Attempt {
            public Assemble {
                order = List.copyOf(order);
            }
        }
        record MatchMark(int index) implements Attempt {}
        record Switches(List<Integer> pressed) implements Attempt {
            public Switches {
                pressed = List.copyOf(pressed);
            }
        }
    }
}
