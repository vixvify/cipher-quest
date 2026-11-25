package com.cipherquest.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainMenuController {
    private static final PseudoClass COMPACT = PseudoClass.getPseudoClass("compact");

    @FXML private StackPane rootPane;
    @FXML private Canvas rainCanvas;
    @FXML private VBox heroCopy;
    @FXML private Region signalSweep;
    @FXML private Region heroArtwork;
    @FXML private Region gateLight;
    @FXML private Button controlsButton;
    @FXML private Button soundButton;

    @FXML
    private void initialize() {
        GameAudio.updateButton(soundButton);
        rootPane.widthProperty().addListener((property, before, width) ->
                rootPane.pseudoClassStateChanged(COMPACT, width.doubleValue() < 960));
        rootPane.setOnMouseMoved(event -> {
            if (!Platform.getPreferences().isReducedMotion() && rootPane.getWidth() > 0 && rootPane.getHeight() > 0) {
                heroArtwork.setTranslateX(10 * (0.5 - event.getSceneX() / rootPane.getWidth()));
                heroArtwork.setTranslateY(8 * (0.5 - event.getSceneY() / rootPane.getHeight()));
            }
        });
        rootPane.setOnMouseExited(event -> {
            heroArtwork.setTranslateX(0);
            heroArtwork.setTranslateY(0);
        });

        rainCanvas.widthProperty().bind(rootPane.widthProperty());
        rainCanvas.heightProperty().bind(rootPane.heightProperty());
        MenuRain rain = new MenuRain(rainCanvas);
        ParallelTransition intro = createIntro();
        TranslateTransition signal = new TranslateTransition(Duration.seconds(2.6), signalSweep);
        signal.setFromX(0);
        signal.setToX(156);
        signal.setAutoReverse(true);
        signal.setCycleCount(TranslateTransition.INDEFINITE);
        FadeTransition light = new FadeTransition(Duration.seconds(3.4), gateLight);
        light.setFromValue(0.30);
        light.setToValue(0.85);
        light.setAutoReverse(true);
        light.setCycleCount(FadeTransition.INDEFINITE);
        rootPane.sceneProperty().addListener((property, before, scene) -> {
            if (scene == null) {
                rain.stop();
                intro.stop();
                signal.stop();
                light.stop();
                return;
            }
            rain.start();
            if (!Platform.getPreferences().isReducedMotion()) {
                intro.playFromStart();
                signal.playFromStart();
                light.playFromStart();
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
        GameAudio.play(GameAudio.Effect.CONNECT);
        ScreenNavigator.startExploration();
    }

    @FXML
    private void onToggleSound() {
        GameAudio.toggle(soundButton);
    }

    @FXML
    private void onShowControls() {
        MissionSupportDialog.showControls(controlsButton);
    }
}
