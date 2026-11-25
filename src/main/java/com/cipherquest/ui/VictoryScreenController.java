package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import com.cipherquest.player.Player;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;

public class VictoryScreenController {

    @FXML private Label endingDescription;
    @FXML private TitledPane endingPanel;
    @FXML private Label scoreLabel;
    @FXML private Label cluesFoundLabel;
    @FXML private Label checkpointsLabel;

    @FXML
    private void initialize() {
        GameEngine engine = GameEngine.getInstance();
        Player player = engine.getPlayer();

        if (player != null) {
            scoreLabel.setText(String.valueOf(player.getScore()));
            animateScore(player.getScore());
            var walk = ScreenNavigator.archiveWalk();
            int evidenceCount = walk == null ? player.getDiscoveredMainClues().size() : walk.collectedEvidenceCount();
            cluesFoundLabel.setText(String.valueOf(evidenceCount));
            checkpointsLabel.setText(player.getCompletedCheckpoints().size() + " / " + engine.getTotalCheckpoints());
        }

        boolean hasEnding = engine.isMainEndingUnlocked() && engine.getCurrentLevel() != null;
        endingPanel.setVisible(hasEnding);
        endingPanel.setManaged(hasEnding);
        if (hasEnding) {
            endingDescription.setText(ThaiTypography.withWordBreaks(engine.getCurrentLevel().getEndingMessage()));
        }
    }

    private void animateScore(int score) {
        if (score == 0) {
            return;
        }
        var displayScore = new SimpleIntegerProperty();
        displayScore.addListener((property, before, value) -> scoreLabel.setText(value.toString()));
        Timeline count = new Timeline(new KeyFrame(Duration.millis(860),
                new KeyValue(displayScore, score, Interpolator.EASE_OUT)));
        scoreLabel.sceneProperty().addListener((property, before, scene) -> {
            if (scene == null) {
                count.stop();
                displayScore.set(score);
            } else if (Platform.isFxApplicationThread() && !Platform.getPreferences().isReducedMotion()) {
                displayScore.set(0);
                scoreLabel.setText("0");
                count.playFromStart();
            }
        });
    }

    @FXML
    private void onReturnToMainMenu() {
        ScreenNavigator.navigateToMainMenu();
    }
}
