package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import com.cipherquest.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VictoryScreenController {

    @FXML private Label endingDescription;
    @FXML private Label scoreLabel;
    @FXML private Label hintsUsedLabel;
    @FXML private Label cluesFoundLabel;
    @FXML private Label checkpointsLabel;

    @FXML
    private void initialize() {
        GameEngine engine = GameEngine.getInstance();
        Player player = engine.getPlayer();

        if (player != null) {
            scoreLabel.setText(String.valueOf(player.getScore()));
            hintsUsedLabel.setText(String.valueOf(player.getHintsUsed()));
            cluesFoundLabel.setText(String.valueOf(player.getDiscoveredMainClues().size()));
            checkpointsLabel.setText(player.getCompletedCheckpoints().size() + " / " + engine.getTotalCheckpoints());
        }

        if (engine.getCurrentLevel() != null) {
            endingDescription.setText(engine.getCurrentLevel().getEndingMessage());
        }
    }

    @FXML
    private void onReturnToMainMenu() {
        ScreenNavigator.navigateToMainMenu();
    }
}
