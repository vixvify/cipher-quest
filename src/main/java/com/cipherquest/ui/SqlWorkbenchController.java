package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameEngine;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.database.DatabaseLevel;
import com.cipherquest.levels.database.QueryResult;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SqlWorkbenchController implements Initializable {

    @FXML private TextArea queryArea;
    @FXML private TableView<List<String>> resultTable;
    @FXML private Label feedbackLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label checkpointLabel;
    @FXML private Label resultStatusLabel;

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
        if (resultStatusLabel != null) {
            resultStatusLabel.setText("Ready");
        }
        rowCountLabel.setText("0 rows");
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
        Level currentLevel = GameEngine.getInstance().getCurrentLevel();

        if (currentLevel instanceof DatabaseLevel dbLevel) {
            QueryResult qr = dbLevel.getLastQueryResult();
            if (qr != null) {
                if (qr.isSuccess()) {
                    populateTable(qr.getColumnNames(), qr.getRows());
                    rowCountLabel.setText(qr.getRowCount() + " rows");
                    if (resultStatusLabel != null) {
                        resultStatusLabel.setText(qr.getRowCount() + " rows retrieved in " + qr.getExecutionTimeMs() + "ms");
                    }

                    if (result.isCorrect()) {
                        feedbackLabel.setStyle("-fx-text-fill: #3ec97e;");
                        feedbackLabel.setText("✔ " + result.getMessage());
                        statusLabel.setText("●  MISSION CLEARED");
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
                    } else {
                        feedbackLabel.setStyle("-fx-text-fill: #94a3b8;");
                        feedbackLabel.setText(result.getMessage());
                    }
                } else {
                    resultTable.getColumns().clear();
                    resultTable.getItems().clear();
                    rowCountLabel.setText("0 rows");
                    if (resultStatusLabel != null) {
                        resultStatusLabel.setText("Error");
                    }
                    feedbackLabel.setStyle("-fx-text-fill: #e05252;");
                    feedbackLabel.setText("✖ " + qr.getErrorMessage());
                }
            }
        }
    }

    private void populateTable(List<String> columnNames, List<List<String>> rows) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        if (columnNames == null || columnNames.isEmpty()) {
            return;
        }

        for (int i = 0; i < columnNames.size(); i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(columnNames.get(i));
            column.setCellValueFactory(param -> {
                List<String> row = param.getValue();
                String val = (row != null && colIndex < row.size()) ? row.get(colIndex) : "";
                return new SimpleStringProperty(val);
            });
            resultTable.getColumns().add(column);
        }

        ObservableList<List<String>> data = FXCollections.observableArrayList(rows);
        resultTable.setItems(data);
    }

    @FXML
    private void onClearQuery() {
        queryArea.clear();
        feedbackLabel.setStyle("-fx-text-fill: #94a3b8;");
        feedbackLabel.setText("");
        rowCountLabel.setText("0 rows");
        if (resultStatusLabel != null) {
            resultStatusLabel.setText("Ready");
        }
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
    }
}
