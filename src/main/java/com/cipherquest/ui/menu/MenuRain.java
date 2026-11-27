package com.cipherquest.ui.menu;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.stream.IntStream;

final class MenuRain extends AnimationTimer {
    private static final long FRAME_INTERVAL = 33_000_000;
    private final Canvas canvas;
    private final Drop[] drops;
    private long lastFrame;

    MenuRain(Canvas canvas) {
        this.canvas = canvas;
        Random random = new Random();
        drops = IntStream.range(0, 85)
                .mapToObj(index -> new Drop(random.nextDouble(), random.nextDouble(),
                        random.nextDouble(0.32, 0.65), random.nextDouble(11, 31),
                        Color.color(0.35, 0.76, 1.0, random.nextDouble(0.10, 0.39))))
                .toArray(Drop[]::new);
    }

    @Override
    public void handle(long now) {
        if (now - lastFrame < FRAME_INTERVAL) {
            return;
        }
        var scene = canvas.getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            return;
        }

        var graphics = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (Platform.getPreferences().isReducedMotion()) {
            if (lastFrame != 0) {
                graphics.clearRect(0, 0, width, height);
                lastFrame = 0;
            }
            return;
        }

        lastFrame = now;
        graphics.clearRect(0, 0, width, height);
        graphics.setLineWidth(1.4);
        double seconds = now / 1_000_000_000.0;
        for (Drop drop : drops) {
            double progress = (drop.phase() + seconds * drop.speed()) % 1;
            double x = drop.column() * width * 0.74 - progress * 72;
            double y = progress * (height + 34);
            graphics.setStroke(drop.color());
            graphics.strokeLine(x, y, x + drop.length() * 0.23, y - drop.length());
        }
    }

    private record Drop(double column, double phase, double speed, double length, Color color) {}
}
