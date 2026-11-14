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
    @FXML private Label promptLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        outputBox.getChildren().clear();
        appendTerminalLine("CipherQuest OS — Linux Terminal Shell v5.15", "term-system-msg");
        appendTerminalLine("Type 'help' for available commands or 'cat readme.txt' for mission details.", "term-system-msg");
        appendTerminalLine("", "term-output");
        updatePrompt();
    }

    @FXML
    private void onExecuteCommand() {
        String command = commandField.getText();
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        String currentPrompt = promptLabel != null ? promptLabel.getText() : "agent@cipherquest:~$ ";
        appendTerminalLine(currentPrompt + " " + command, "term-prompt");
        commandField.clear();

        AnswerResult result = GameEngine.getInstance().submitAnswer(command);
        String output = result.getMessage();

        if ("__CLEAR__".equals(output)) {
            outputBox.getChildren().clear();
        } else if (output != null && !output.isEmpty()) {
            appendTerminalLine(output, result.isCorrect() ? "term-output" : "term-output");
        }

        updatePrompt();

        if (result.isCorrect()) {
            statusLabel.setText("●  ACCESS GRANTED");
            statusLabel.setStyle("-fx-text-fill: #3ec97e;");

            PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
            pause.setOnFinished(e -> {
                if (result.getStatus() == AnswerResult.Status.GAME_CLEARED) {
                    ScreenNavigator.navigateToMainMenu();
                } else {
                    GameEngine.getInstance().advanceToNextCheckpoint();
                    ScreenNavigator.navigateToCurrentLevel();
                }
            });
            pause.play();
        }

        scrollToBottom();
    }

    private void updatePrompt() {
        if (promptLabel == null) return;
        Level level = GameEngine.getInstance().getCurrentLevel();
        String path = "/home/agent";
        if (level instanceof LinuxLevel linux) {
            path = linux.getTerminal().getCurrentPath();
        }

        if (path.equals("/home/agent")) {
            path = "~";
        } else if (path.startsWith("/home/agent/")) {
            path = "~" + path.substring("/home/agent".length());
        }

        promptLabel.setText("agent@cipherquest:" + path + "$");
    }

    private void appendTerminalLine(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        outputBox.getChildren().add(label);
    }

    private void scrollToBottom() {
        if (outputScrollPane != null) {
            outputScrollPane.layout();
            outputScrollPane.setVvalue(1.0);
        }
    }
}
