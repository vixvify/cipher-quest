package com.cipherquest.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainMenuController {
    private static final PseudoClass COMPACT = PseudoClass.getPseudoClass("compact");

    @FXML private StackPane rootPane;
    @FXML private Canvas rainCanvas;
    @FXML private VBox heroCopy;

    @FXML
    private void initialize() {
        rootPane.widthProperty().addListener((property, before, width) ->
                rootPane.pseudoClassStateChanged(COMPACT, width.doubleValue() < 960));

        rainCanvas.widthProperty().bind(rootPane.widthProperty());
        rainCanvas.heightProperty().bind(rootPane.heightProperty());
        MenuRain rain = new MenuRain(rainCanvas);
        ParallelTransition intro = createIntro();
        rootPane.sceneProperty().addListener((property, before, scene) -> {
            if (scene == null) {
                rain.stop();
                intro.stop();
                return;
            }
            rain.start();
            if (!Platform.getPreferences().isReducedMotion()) {
                intro.playFromStart();
            }
        });
    }

    private ParallelTransition createIntro() {
        Duration duration = Duration.millis(460);
        FadeTransition fade = new FadeTransition(duration, heroCopy);
        fade.setFromValue(0.55);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(duration, heroCopy);
        slide.setFromY(15);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        return new ParallelTransition(fade, slide);
    }

    @FXML
    private void onStartGame() {
        ScreenNavigator.SESSION.start();
        ScreenNavigator.navigateToCurrentLevel();
    }
}
