package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameSession;
import com.cipherquest.levels.database.QueryResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class SqlWorkbenchController extends BaseMissionController {

    @FXML private TextArea queryArea;
    @FXML private TableView<List<String>> resultTable;
    @FXML private VBox schemaTables;
    @FXML private Button runButton;
    @FXML private Button clearButton;
    @FXML private Button knowledgeButton;
    @FXML private Button cluesButton;
    @FXML private Label feedbackLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label resultStatusLabel;
    @FXML private Label taskLabel;

    @FXML
    private void initialize() {
        runButton.setTooltip(new Tooltip("Ctrl + Enter"));
        queryArea.textProperty().addListener((property, before, text) ->
                ThaiTypography.useThaiFontWhenNeeded(queryArea, text));
        queryArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                runButton.fire();
                event.consume();
            }
        });
        initializeMissionHeader();
        updateClueCount();
        knowledgeButton.setDisable(ScreenNavigator.SESSION.currentSqlTopics().isEmpty());
        ScreenNavigator.SESSION.sqlBrief().ifPresent(brief -> {
            taskLabel.setText(ThaiTypography.withWordBreaks(brief.task()));
            for (String table : brief.tables()) {
                Label name = new Label(table);
                name.getStyleClass().add("sql-table-item");
                schemaTables.getChildren().add(name);
            }
        });
    }

    @FXML
    private void onOpenKnowledge() {
        MissionSupportDialog.showKnowledge(knowledgeButton, ScreenNavigator.SESSION.currentSqlTopics());
    }

    @FXML
    private void onOpenClues() {
        MissionSupportDialog.showClues(cluesButton, ScreenNavigator.SESSION.discoveredClues());
    }

    private void updateClueCount() {
        cluesButton.setText("เบาะแส " + ScreenNavigator.SESSION.discoveredClues().size());
    }

    @FXML
    private void onRunQuery() {
        String query = queryArea.getText();
        if (query.isBlank()) {
            Feedback.show(feedbackLabel, "กรุณาพิมพ์ SQL Query ก่อนกดรัน", false, true);
            return;
        }

        GameSession.SqlSubmission submission = ScreenNavigator.SESSION.runQuery(query);
        AnswerResult answer = submission.answer();
        QueryResult result = submission.result();
        if (result == null) {
            Feedback.show(feedbackLabel, answer.getMessage(), false, true);
            return;
        }
        if (!result.isSuccess()) {
            clearResults("Error");
            Feedback.show(feedbackLabel, result.getErrorMessage(), false, true);
            return;
        }

        populateTable(result.getColumnNames(), result.getRows());
        rowCountLabel.setText(result.getRowCount() + " rows");
        resultStatusLabel.setText(result.getExecutionTimeMs() + " ms");
        Feedback.show(feedbackLabel, answer.getMessage(), answer.isCorrect(), false);
        if (answer.isCorrect()) {
            Feedback.show(statusLabel, "●  MISSION CLEARED", true, false);
            queryArea.setEditable(false);
            runButton.setDisable(true);
            clearButton.setDisable(true);
            knowledgeButton.setDisable(true);
            updateClueCount();
            cluesButton.setDisable(true);
            proceedAfterFeedback(queryArea, answer, Duration.seconds(2));
        }
    }

    private void populateTable(List<String> columnNames, List<List<String>> rows) {
        resultTable.getColumns().clear();
        for (int i = 0; i < columnNames.size(); i++) {
            int index = i;
            TableColumn<List<String>, String> column = new TableColumn<>(columnNames.get(i));
            column.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(index)));
            resultTable.getColumns().add(column);
        }
        resultTable.getItems().setAll(rows);
    }

    private void clearResults(String status) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        rowCountLabel.setText("0 rows");
        resultStatusLabel.setText(status);
    }

    @FXML
    private void onClearQuery() {
        queryArea.clear();
        clearResults("Ready");
        Feedback.show(feedbackLabel, "", false, false);
    }
}
