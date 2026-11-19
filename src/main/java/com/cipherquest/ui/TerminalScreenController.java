package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TerminalScreenController {
    private final List<String> history = new ArrayList<>();
    private int historyPosition;
    private String draft = "";

    @FXML private TextField commandField;
    @FXML private Button executeButton;
    @FXML private VBox outputBox;
    @FXML private ScrollPane outputScrollPane;
    @FXML private Label statusLabel;
    @FXML private Label promptLabel;
    @FXML private Label checkpointLabel;
    @FXML private Label missionTitle;
    @FXML private Label missionLabel;

    @FXML
    private void initialize() {
        ScreenNavigator.SESSION.currentMission().ifPresent(mission -> {
            missionTitle.setText(mission.title());
            missionLabel.setText(mission.description());
            checkpointLabel.setText(ScreenNavigator.checkpointText(mission));
        });
        commandField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                showHistory(event.getCode() == KeyCode.UP ? -1 : 1);
                event.consume();
            }
        });
        promptLabel.setTooltip(new Tooltip());
        updatePrompt();
    }

    @FXML
    private void onExecuteCommand() {
        String command = commandField.getText();
        if (command.isBlank() || !commandField.isEditable()) {
            return;
        }

        appendTerminalLine(promptLabel.getText() + " " + command, "term-prompt");
        history.add(command);
        historyPosition = history.size();
        draft = "";
        commandField.clear();
        setCommandPending(true);

        Task<GameSession.TerminalSubmission> task = new Task<>() {
            @Override
            protected GameSession.TerminalSubmission call() {
                return ScreenNavigator.SESSION.runCommand(command);
            }
        };
        task.setOnSucceeded(event -> showSubmission(task.getValue()));
        task.setOnFailed(event -> {
            appendTerminalLine("Terminal: unable to process command.", "term-output");
            setCommandPending(false);
        });
        Thread.startVirtualThread(task);
    }

    private void showSubmission(GameSession.TerminalSubmission submission) {
        AnswerResult result = submission.answer();
        String output = result.getMessage();

        if (submission.clearOutput()) {
            outputBox.getChildren().clear();
        }
        if (output != null && !output.isEmpty()) {
            appendTerminalLine(output, "term-output");
        }

        updatePrompt();

        if (result.isCorrect()) {
            Feedback.show(statusLabel, "●  ACCESS GRANTED", true, false);
            commandField.setDisable(true);
            ScreenNavigator.continueAfterFeedback(commandField, result, Duration.seconds(2));
        } else {
            setCommandPending(false);
        }
        Platform.runLater(() -> outputScrollPane.setVvalue(1.0));
    }

    private void setCommandPending(boolean pending) {
        commandField.setEditable(!pending);
        executeButton.setDisable(pending);
        Feedback.show(statusLabel, pending ? "RUNNING" : "READY", false, false);
        if (!pending) {
            commandField.requestFocus();
        }
    }

    private void showHistory(int direction) {
        if (!commandField.isEditable() || history.isEmpty()) {
            return;
        }
        if (historyPosition == history.size()) {
            draft = commandField.getText();
        }
        historyPosition = Math.clamp(historyPosition + direction, 0, history.size());
        commandField.setText(historyPosition == history.size() ? draft : history.get(historyPosition));
        commandField.end();
    }

    private void updatePrompt() {
        String directory = ScreenNavigator.SESSION.terminalPath();
        promptLabel.setText(directory + " $");
        promptLabel.getTooltip().setText("agent@cipherquest:" + directory);
    }

    private void appendTerminalLine(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        outputBox.getChildren().add(label);
        if (outputBox.getChildren().size() > 250) {
            outputBox.getChildren().removeFirst();
        }
    }
}
