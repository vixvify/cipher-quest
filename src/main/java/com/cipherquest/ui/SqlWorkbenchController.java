package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameEngine;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.database.DatabaseLevel;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SqlWorkbenchController implements Initializable {

    @FXML private TextArea queryArea;
    @FXML private TableView<Object> resultTable;
    @FXML private Label feedbackLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label checkpointLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Level currentLevel = GameEngine.getInstance().getCurrentLevel();
        if (currentLevel != null) {
            checkpointLabel.setText("CHECKPOINT " + currentLevel.getCheckpointId() + " / " + GameEngine.getInstance().getTotalCheckpoints());
            if (currentLevel instanceof DatabaseLevel dbLevel && dbLevel.getTask() != null) {
                feedbackLabel.setText("โจทย์: " + dbLevel.getTask());
            } else {
                feedbackLabel.setText(currentLevel.getDescription());
            }
        }
    }

    @FXML
    private void onRunQuery() {
        String query = queryArea.getText();
        if (query == null || query.trim().isEmpty()) {
            feedbackLabel.setStyle("-fx-text-fill: #e05252;");
            feedbackLabel.setText("กรุณาพิมพ์ SQL Query ก่อนกดรัน");
            return;
        }

        AnswerResult result = GameEngine.getInstance().submitAnswer(query);

        if (result.isCorrect()) {
            feedbackLabel.setStyle("-fx-text-fill: #3ec97e;");
            feedbackLabel.setText("✔ " + result.getMessage());

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                if (result.getStatus() == AnswerResult.Status.GAME_CLEARED) {
                    ScreenNavigator.navigateToMainMenu();
                } else {
                    GameEngine.getInstance().advanceToNextCheckpoint();
                    ScreenNavigator.navigateToCurrentLevel();
                }
            });
            pause.play();
        } else {
            feedbackLabel.setStyle("-fx-text-fill: #e05252;");
            feedbackLabel.setText("✖ " + result.getMessage());
        }
    }

    @FXML
    private void onClearQuery() {
        queryArea.clear();
        feedbackLabel.setText("");
        rowCountLabel.setText("0 rows");
    }
}
