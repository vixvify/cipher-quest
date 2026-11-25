package com.cipherquest.ui;

import com.cipherquest.core.GameSession.CampaignProgress;
import com.cipherquest.core.GameSession.CheckpointStatus;
import com.cipherquest.exploration.ArchiveMap;
import com.cipherquest.exploration.ArchiveWalk;
import com.cipherquest.exploration.FieldActivity;
import com.cipherquest.exploration.FieldNote;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

final class ArchiveRenderer {
    private static final int TILE = 24;
    private static final Color LIME = Color.web("#c9fb82");
    private static final Color CYAN = Color.web("#78cbe5");
    private static final Color LOCKED = Color.web("#5e6487");
    private static final Font STATION_FONT = Font.font("Consolas", FontWeight.BOLD, 10);

    private final Canvas canvas;
    private final GraphicsContext graphics;

    ArchiveRenderer(Canvas canvas) {
        this.canvas = canvas;
        graphics = canvas.getGraphicsContext2D();
        graphics.setImageSmoothing(false);
    }

    void draw(ArchiveWalk walk, CampaignProgress progress, int activeCheckpoint, Optional<ArchiveMap.Station> nearby,
              double seconds, boolean reduceMotion) {
        var map = walk.map();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        double mapWidth = map.width() * TILE;
        double mapHeight = map.height() * TILE;
        double scale = Math.min(width / mapWidth, height / mapHeight);
        double animationTime = reduceMotion ? 0 : seconds;
        rectangle(Color.web("#0d1020"), 0, 0, width, height);
        graphics.save();
        graphics.translate((width - mapWidth * scale) / 2, (height - mapHeight * scale) / 2);
        graphics.scale(scale, scale);
        for (int row = 0; row < map.height(); row++) {
            for (int column = 0; column < map.width(); column++) {
                drawTile(map.tileAt(column, row), column, row, progress, animationTime);
            }
        }
        for (var station : map.stations()) {
            CheckpointStatus status = progress.status(station.checkpoint());
            drawStation(station, status, nearby.filter(station::equals).isPresent(),
                    animationTime);
        }
        for (var note : walk.uncollectedNotes(activeCheckpoint)) {
            drawActivity(note, animationTime);
        }
        drawPlayer(walk, reduceMotion);
        graphics.restore();
    }

    private void drawTile(char tile, int column, int row, CampaignProgress progress, double seconds) {
        int x = column * TILE;
        int y = row * TILE;
        drawFloor(x, y, column, row, tile == ':' || tile == '@' || ArchiveMap.gateCheckpoint(tile) != 0);
        switch (tile) {
            case '#' -> drawWall(x, y);
            case 'W' -> drawWindow(x, y, seconds);
            case 'B' -> drawShelf(x, y);
            case 'R' -> drawRack(x, y);
            case 'P' -> drawPlant(x, y);
            case 'A', 'G' -> drawGate(x, y, progress.status(ArchiveMap.gateCheckpoint(tile)) != CheckpointStatus.LOCKED);
            default -> {}
        }
    }

    private void drawFloor(int x, int y, int column, int row, boolean corridor) {
        Color floor;
        if (corridor) {
            floor = Color.web("#313351");
        } else if (column > 20) {
            floor = Color.web((column + row) % 2 == 0 ? "#25223a" : "#28243c");
        } else {
            floor = Color.web((column + row) % 2 == 0 ? "#1a2035" : "#1c2239");
        }
        rectangle(floor, x, y, TILE, TILE);
        if (corridor) {
            rectangle(Color.web("#54436e"), x, y + 3, TILE, 1);
            rectangle(Color.web("#54436e"), x, y + 20, TILE, 1);
            rectangle(Color.web("#8070a3"), x + 10, y + 11, 4, 1);
        } else {
            rectangle(Color.web("#161a2c"), x, y + 23, TILE, 1);
            rectangle(Color.web("#252942"), x + 23, y, 1, TILE);
            if ((column * 7 + row * 3) % 11 == 0) {
                rectangle(Color.web("#3c3657"), x + 3, y + 5, 6, 1);
            }
        }
    }

    private void drawWall(int x, int y) {
        rectangle(Color.web("#161325"), x, y, TILE, TILE);
        rectangle(Color.web("#302744"), x, y, TILE, 15);
        rectangle(Color.web("#4b3b62"), x, y, TILE, 2);
        rectangle(Color.web("#251e37"), x + 22, y + 2, 2, 13);
        rectangle(Color.web("#21182f"), x, y + 15, TILE, 4);
    }

    private void drawWindow(int x, int y, double time) {
        drawWall(x, y);
        rectangle(Color.web("#17253f"), x + 2, y + 3, 20, 15);
        rectangle(Color.web("#245271"), x + 3, y + 13, 18, 3);
        rectangle(Color.web("#4f76a5"), x + 2, y + 18, 20, 2);
        for (int index = 0; index < 3; index++) {
            int lineX = x + 5 + index * 6;
            double lineY = y + 4 + (index * 3 + time * 11 + x) % 8;
            graphics.setStroke(Color.web("#719ed0", 0.65));
            graphics.strokeLine(lineX, lineY + 3, lineX + 1, lineY);
        }
    }

    private void drawShelf(int x, int y) {
        rectangle(Color.web("#110f20"), x + 1, y + 4, 22, 19);
        rectangle(Color.web("#755372"), x + 2, y + 2, 20, 17);
        rectangle(Color.web("#392944"), x + 4, y + 5, 16, 11);
        for (int book = 0; book < 4; book++) {
            Color cover = switch (book) {
                case 0 -> Color.web("#667193");
                case 1 -> Color.web("#b48687");
                case 2 -> Color.web("#7771a8");
                default -> Color.web("#527f80");
            };
            rectangle(cover, x + 5 + book * 4, y + 6 + book % 2, 3, 8 - book % 2);
        }
        rectangle(Color.web("#a47993"), x + 2, y + 17, 20, 2);
    }

    private void drawRack(int x, int y) {
        rectangle(Color.web("#0f1323"), x + 3, y + 5, 18, 17);
        rectangle(Color.web("#4b4662"), x + 3, y + 3, 18, 16);
        rectangle(Color.web("#211d34"), x + 5, y + 5, 14, 11);
        for (int slot = 0; slot < 3; slot++) {
            rectangle(Color.web("#685e82"), x + 6, y + 6 + slot * 3, 11, 1);
            rectangle(CYAN, x + 14, y + 6 + slot * 3, 2, 1);
        }
    }

    private void drawPlant(int x, int y) {
        rectangle(Color.web("#171325"), x + 6, y + 13, 12, 8);
        rectangle(Color.web("#766078"), x + 8, y + 12, 8, 7);
        rectangle(Color.web("#398280"), x + 5, y + 6, 5, 7);
        rectangle(Color.web("#56a798"), x + 10, y + 3, 4, 11);
        rectangle(Color.web("#397772"), x + 15, y + 6, 4, 7);
        rectangle(Color.web("#8cc39a"), x + 11, y + 3, 2, 4);
    }

    private void drawGate(int x, int y, boolean open) {
        rectangle(Color.web("#9584be"), x + 3, y, 18, 3);
        rectangle(Color.web("#9584be"), x + 3, y + 21, 18, 3);
        rectangle(open ? LIME : Color.web("#d88892"), x + 18, y + 1, 3, 2);
        if (!open) {
            rectangle(Color.web("#332642"), x + 8, y + 3, 8, 18);
            for (int row = 5; row < 21; row += 4) {
                rectangle(Color.web("#805777"), x + 9, y + row, 6, 1);
            }
        }
    }

    private void drawStation(ArchiveMap.Station station, CheckpointStatus state, boolean nearby, double seconds) {
        int x = station.column() * TILE;
        int y = station.row() * TILE;
        Color status = switch (state) {
            case AVAILABLE -> LIME;
            case COMPLETED -> CYAN;
            case LOCKED -> LOCKED;
        };
        if (state == CheckpointStatus.AVAILABLE || nearby) {
            double pulse = 0.36 + 0.13 * Math.sin(seconds * 4);
            rectangle(status.deriveColor(0, 1, 1, pulse), x - 2, y - 2, TILE + 4, TILE + 4);
        }
        rectangle(Color.web("#12101d"), x + 2, y + 6, 21, 16);
        rectangle(Color.web("#514561"), x + 1, y + 2, 21, 16);
        rectangle(Color.web("#10192b"), x + 4, y + 4, 15, 10);
        rectangle(status.deriveColor(0, 1, 0.78, 1), x + 3, y + 16, 17, 2);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(STATION_FONT);
        graphics.setFill(status);
        graphics.fillText(Integer.toString(station.checkpoint()), x + 11.5, y + 12);
        if (state == CheckpointStatus.AVAILABLE) {
            rectangle(LIME, x + 10, y - 5 - Math.sin(seconds * 4), 4, 2);
        }
    }

    private void drawActivity(FieldNote note, double seconds) {
        double x = note.position().x() * TILE;
        double y = note.position().y() * TILE;
        boolean source = note.activity() instanceof FieldActivity.Source;
        Color accent = source ? Color.web("#ffdf98") : CYAN;
        double glow = 0.24 + 0.10 * Math.sin(seconds * 4);
        rectangle(accent.deriveColor(0, 1, 1, glow), x - 11, y - 9, 22, 18);
        rectangle(Color.web("#47405d"), x - 8, y - 5, 16, 11);
        rectangle(accent, x - 7, y - 6, 14, 9);
        if (source) {
            rectangle(Color.web("#a67753"), x - 5, y - 4, 9, 1);
            rectangle(Color.web("#a67753"), x - 5, y - 1, 6, 1);
        } else {
            rectangle(Color.web("#203a50"), x - 4, y - 4, 8, 5);
            rectangle(accent, x - 2, y + 4, 4, 2);
        }
        rectangle(accent, x - 1, y - 12 - Math.sin(seconds * 4), 3, 2);
    }

    private void drawPlayer(ArchiveWalk walk, boolean reduceMotion) {
        double x = Math.floor(walk.position().x() * TILE);
        double y = Math.floor(walk.position().y() * TILE);
        int stride = reduceMotion ? 0 : (int) Math.round(Math.sin(walk.distanceWalked() * 8) * 2);
        graphics.setFill(Color.web("#0e1324", 0.74));
        graphics.fillOval(x - 8, y + 5, 16, 6);
        rectangle(Color.web("#24253c"), x - 5, y + 4 - stride, 4, 7);
        rectangle(Color.web("#24253c"), x + 1, y + 4 + stride, 4, 7);
        rectangle(Color.web("#df9c92"), x - 6, y - 5, 12, 11);
        rectangle(Color.web("#a8617e"), x - 6, y + 3, 12, 3);
        rectangle(Color.web("#665894"), x - 6, y - 10, 12, 9);
        rectangle(Color.web("#a89bc5"), x - 4, y - 12, 8, 3);
        if (walk.facing() != ArchiveWalk.Facing.NORTH) {
            int visorOffset = switch (walk.facing()) {
                case EAST -> 0;
                case WEST -> -6;
                default -> -3;
            };
            rectangle(CYAN, x + visorOffset, y - 6, 6, 2);
        }
        rectangle(LIME, x + 3, y - 1, 2, 3);
    }

    private void rectangle(Color color, double x, double y, double width, double height) {
        graphics.setFill(color);
        graphics.fillRect(x, y, width, height);
    }
}
