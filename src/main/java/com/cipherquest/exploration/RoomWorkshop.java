package com.cipherquest.exploration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract sealed class RoomWorkshop {
    private final Kind kind;

    RoomWorkshop(Kind kind) {
        this.kind = kind;
    }

    public final Kind kind() {
        return kind;
    }

    abstract boolean solved();
    public abstract int completed();
    public abstract int total();
    public abstract Signal signal();
    public abstract List<Marker> markers();

    void tick(double seconds, Tile player) {}
    boolean use(Tile tile) { return false; }
    boolean blocks(Tile tile) { return false; }
    boolean push(Tile tile, int horizontal, int vertical, Predicate<Tile> walkable) { return false; }

    public Optional<Beam> beam() {
        return Optional.empty();
    }

    public Optional<Marker> nearbySwitch(ArchiveMap.Position player) {
        return markers().stream()
                .filter(marker -> marker.type() == MarkerType.SWITCH && !marker.active())
                .filter(marker -> Math.hypot(player.x() - marker.tile().x() - 0.5,
                        player.y() - marker.tile().y() - 0.5) < 1.12)
                .min(java.util.Comparator.comparingDouble(marker -> Math.hypot(player.x() - marker.tile().x() - 0.5,
                        player.y() - marker.tile().y() - 0.5)));
    }

    public enum Kind { POWER, CARGO, VAULT }
    public enum Signal { READY, CONNECTED, WRONG_ORDER, SCANNER_RESET, BLOCKED, MOVED }
    public enum MarkerType { SWITCH, CRATE, DOCK }

    public record Tile(int x, int y) {
        static Tile under(ArchiveMap.Position position) {
            return new Tile((int) Math.floor(position.x()), (int) Math.floor(position.y()));
        }

        Tile translate(int horizontal, int vertical) {
            return new Tile(x + horizontal, y + vertical);
        }
    }

    public record Bounds(Tile min, Tile max) {
        boolean includes(Tile tile) {
            return tile.x() >= min.x() && tile.x() <= max.x() && tile.y() >= min.y() && tile.y() <= max.y();
        }
    }

    public record Marker(String label, Tile tile, MarkerType type, boolean active) {}
    public record Device(String label, Tile tile) {}
    public record Freight(Device crate, Device dock) {}

    public record Beam(int left, int right, int row) {
        boolean crosses(Tile tile) {
            return tile.y() == row && tile.x() >= left && tile.x() <= right;
        }
    }

    public sealed interface Layout {
        RoomWorkshop begin();
        List<Tile> fixtures();

        record Switches(Kind kind, List<Device> sequence, Bounds sweep) implements Layout {
            public Switches {
                sequence = List.copyOf(sequence);
                if (sequence.isEmpty() || kind == Kind.CARGO || (kind == Kind.VAULT && sweep == null)) {
                    throw new IllegalArgumentException("Invalid switchboard layout");
                }
            }

            @Override
            public RoomWorkshop begin() {
                return new Switchboard(this);
            }

            @Override
            public List<Tile> fixtures() {
                return sequence.stream().map(Device::tile).toList();
            }
        }

        record LoadingDock(Bounds area, List<Freight> freight) implements Layout {
            public LoadingDock {
                freight = List.copyOf(freight);
                if (freight.isEmpty() || freight.stream().anyMatch(load ->
                        !area.includes(load.crate().tile()) || !area.includes(load.dock().tile()))) {
                    throw new IllegalArgumentException("Invalid freight layout");
                }
            }

            @Override
            public RoomWorkshop begin() {
                return new Cargo(this);
            }

            @Override
            public List<Tile> fixtures() {
                return freight.stream().flatMap(load -> java.util.stream.Stream.of(
                        load.crate().tile(), load.dock().tile())).toList();
            }
        }
    }

    static final class Switchboard extends RoomWorkshop {
        private final Layout.Switches layout;
        private int turnedOn;
        private double elapsed;
        private Signal signal = Signal.READY;

        Switchboard(Layout.Switches layout) {
            super(layout.kind());
            this.layout = layout;
        }

        @Override
        void tick(double seconds, Tile player) {
            elapsed += Math.max(0, seconds);
            if (!solved() && turnedOn > 0 && beam().filter(beam -> beam.crosses(player)).isPresent()) {
                turnedOn = 0;
                signal = Signal.SCANNER_RESET;
            }
        }

        @Override
        boolean use(Tile tile) {
            int index = layout.fixtures().indexOf(tile);
            if (index < turnedOn || solved()) {
                return false;
            }
            if (index != turnedOn) {
                signal = Signal.WRONG_ORDER;
                return false;
            }
            turnedOn++;
            signal = Signal.CONNECTED;
            return true;
        }

        @Override
        public Optional<Beam> beam() {
            if (layout.sweep() == null) {
                return Optional.empty();
            }
            var sweep = layout.sweep();
            int range = sweep.max().y() - sweep.min().y();
            int cycle = Math.max(1, range * 2);
            int phase = ((int) (elapsed / 0.72)) % cycle;
            int row = sweep.min().y() + Math.min(phase, cycle - phase);
            return Optional.of(new Beam(sweep.min().x(), sweep.max().x(), row));
        }

        @Override boolean solved() { return turnedOn == total(); }
        @Override public int completed() { return turnedOn; }
        @Override public int total() { return layout.sequence().size(); }
        @Override public Signal signal() { return signal; }

        @Override
        public List<Marker> markers() {
            List<Marker> markers = new ArrayList<>();
            for (int index = 0; index < layout.sequence().size(); index++) {
                Device device = layout.sequence().get(index);
                markers.add(new Marker(device.label(), device.tile(), MarkerType.SWITCH, index < turnedOn));
            }
            return List.copyOf(markers);
        }
    }

    static final class Cargo extends RoomWorkshop {
        private final Layout.LoadingDock layout;
        private final List<Tile> crates;
        private Signal signal = Signal.READY;

        Cargo(Layout.LoadingDock layout) {
            super(Kind.CARGO);
            this.layout = layout;
            crates = new ArrayList<>(layout.freight().stream().map(load -> load.crate().tile()).toList());
        }

        @Override boolean blocks(Tile tile) { return crates.contains(tile); }

        @Override
        boolean push(Tile tile, int horizontal, int vertical, Predicate<Tile> walkable) {
            int crate = crates.indexOf(tile);
            if (crate < 0 || Math.abs(horizontal) + Math.abs(vertical) != 1) {
                return false;
            }
            Tile destination = tile.translate(horizontal, vertical);
            if (!layout.area().includes(destination) || !walkable.test(destination) || blocks(destination)) {
                signal = Signal.BLOCKED;
                return false;
            }
            crates.set(crate, destination);
            signal = Signal.MOVED;
            return true;
        }

        @Override boolean solved() { return completed() == total(); }
        @Override public int total() { return crates.size(); }
        @Override public Signal signal() { return signal; }

        @Override
        public int completed() {
            int matches = 0;
            for (int index = 0; index < crates.size(); index++) {
                if (correctlyParked(index)) {
                    matches++;
                }
            }
            return matches;
        }

        private boolean correctlyParked(int index) {
            return crates.get(index).equals(layout.freight().get(index).dock().tile());
        }

        @Override
        public List<Marker> markers() {
            List<Marker> marks = new ArrayList<>();
            for (int index = 0; index < crates.size(); index++) {
                Freight item = layout.freight().get(index);
                boolean parked = correctlyParked(index);
                marks.add(new Marker(item.dock().label(), item.dock().tile(), MarkerType.DOCK, parked));
                marks.add(new Marker(item.crate().label(), crates.get(index), MarkerType.CRATE, parked));
            }
            return List.copyOf(marks);
        }
    }
}
