package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import com.cipherquest.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

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

    @FXML
    private void onReturnToMainMenu() {
        ScreenNavigator.navigateToMainMenu();
    }
}
