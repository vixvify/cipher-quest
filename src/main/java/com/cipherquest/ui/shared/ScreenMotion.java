package com.cipherquest.ui.shared;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

public final class ScreenMotion {
    private ScreenMotion() {}

    public static void reveal(Node node, double rise, double opacity, int milliseconds) {
        if (node.getScene() == null || !Platform.isFxApplicationThread() || Platform.getPreferences().isReducedMotion()) {
            return;
        }
        Duration duration = Duration.millis(milliseconds);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(opacity);
        fade.setToValue(1);
        TranslateTransition position = new TranslateTransition(duration, node);
        position.setFromY(rise);
        position.setToY(0);
        position.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, position).play();
    }
}
