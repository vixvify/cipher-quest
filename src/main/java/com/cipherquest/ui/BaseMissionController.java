package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public abstract class BaseMissionController {

    @FXML protected Label checkpointLabel;
    @FXML protected Label missionTitle;
    @FXML protected Label missionLabel;

    protected void initializeMissionHeader() {
        ScreenNavigator.SESSION.currentMission().ifPresent(mission -> {
            if (checkpointLabel != null) {
                checkpointLabel.setText(ScreenNavigator.checkpointText(mission));
            }
            if (missionTitle != null) {
                missionTitle.setText(mission.title());
            }
            if (missionLabel != null) {
                missionLabel.setText(mission.description());
            }
        });
    }

    protected void proceedAfterFeedback(Node node, AnswerResult result, Duration delay) {
        ScreenNavigator.continueAfterFeedback(node, result, delay);
    }
}
