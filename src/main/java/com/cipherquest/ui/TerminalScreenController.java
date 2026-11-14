package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameEngine;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.linux.LinuxLevel;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class TerminalScreenController implements Initializable {

    @FXML private TextField commandField;
    @FXML private VBox outputBox;
    @FXML private ScrollPane outputScrollPane;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Level currentLevel = GameEngine.getInstance().getCurrentLevel();
        if (currentLevel != null) {
            appendTerminalLine("[TASK] " + currentLevel.getDescription(), "term-system-msg");
            if (currentLevel instanceof LinuxLevel linux && linux.getTask() != null) {
                appendTerminalLine("[INFO] " + linux.getTask(), "term-system-msg");
            }
        }
    }

    @FXML
    private void onExecuteCommand() {
        String command = commandField.getText();
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        appendTerminalLine("agent@cipherquest:~$ " + command, "term-prompt");
        commandField.clear();

        AnswerResult result = GameEngine.getInstance().submitAnswer(command);

        if (result.isCorrect()) {
            Level currentLevel = GameEngine.getInstance().getCurrentLevel();
            if (currentLevel instanceof LinuxLevel linux && linux.getSimulatedOutput() != null) {
                appendTerminalLine(linux.getSimulatedOutput(), "term-output");
            }
            appendTerminalLine("✔ " + result.getMessage(), "term-output");
            statusLabel.setText("●  ACCESS GRANTED");
            statusLabel.setStyle("-fx-text-fill: #3ec97e;");

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
            appendTerminalLine("bash: " + command + ": command failed or incorrect flag", "term-output-error");
            appendTerminalLine("✖ " + result.getMessage(), "term-output-error");
        }

        scrollToBottom();
    }

    private void appendTerminalLine(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        outputBox.getChildren().add(label);
    }

    private void scrollToBottom() {
        if (outputScrollPane != null) {
            outputScrollPane.layout();
            outputScrollPane.setVvalue(1.0);
        }
    }
}
