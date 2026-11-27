package com.cipherquest.ui.exploration;

import com.cipherquest.ui.shared.MissionSupportDialog;
import com.cipherquest.ui.shared.ThaiTypography;

import com.cipherquest.exploration.FieldActivity;
import com.cipherquest.exploration.FieldNote;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

final class FieldActivityDialog {
    private static final PseudoClass MARKED = PseudoClass.getPseudoClass("marked");

    private FieldActivityDialog() {}

    static void showSource(Node owner, FieldNote note, Runnable onClose) {
        Dialog<Void> dialog = MissionSupportDialog.createDialog(owner, note.title(), "");
        Label evidence = MissionSupportDialog.paragraph(note.text());
        Label saved = new Label("✓  เพิ่มข้อมูลลงในสมุดภาคสนามแล้ว");
        saved.getStyleClass().add("field-saved-status");
        saved.setWrapText(true);
        dialog.getDialogPane().setContent(new VBox(12, evidence, new Separator(), saved));
        dialog.setOnHidden(event -> onClose.run());
        dialog.show();
    }

    static void show(Node owner, FieldNote note, List<FieldNote> journal,
                     Predicate<FieldActivity.Attempt> submit, Runnable onClose) {
        Dialog<Void> dialog = MissionSupportDialog.createDialog(owner, note.title(), "");
        dialog.getDialogPane().setPrefWidth(605);
        ButtonType confirmType = new ButtonType("ยืนยัน", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(confirmType);
        Button confirm = (Button) dialog.getDialogPane().lookupButton(confirmType);
        confirm.getStyleClass().add("primary-button");
        Label feedback = MissionSupportDialog.paragraph("");
        String task = switch (note.activity()) {
            case FieldActivity.RecordDesk desk -> desk.task();
            case FieldActivity.Signal signal -> signal.task();
            case FieldActivity.Circuit circuit -> circuit.task();
            default -> throw new IllegalArgumentException("Activity has no dialog");
        };
        Node controls = switch (note.activity()) {
            case FieldActivity.RecordDesk desk -> deskPanel(desk, confirm, dialog, submit, feedback);
            case FieldActivity.Signal signal -> signalPanel(signal, confirm, dialog, submit, feedback);
            case FieldActivity.Circuit circuit -> circuitPanel(circuit, confirm, dialog, submit, feedback);
            default -> throw new IllegalArgumentException("Activity has no dialog");
        };
        VBox content = new VBox(12, MissionSupportDialog.paragraph(task), controls, journalPane(journal));
        content.setPadding(new Insets(0, 7, 6, 0));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(385);
        feedback.managedProperty().bind(feedback.textProperty().isNotEmpty());
        dialog.getDialogPane().setContent(new VBox(8, scroll, feedback));
        dialog.setOnHidden(event -> onClose.run());
        dialog.show();
    }

    private static TitledPane journalPane(List<FieldNote> journal) {
        VBox entries = new VBox(8);
        for (FieldNote note : journal) {
            Label heading = new Label(ThaiTypography.withWordBreaks(note.title()));
            heading.getStyleClass().add("support-section-title");
            heading.setWrapText(true);
            entries.getChildren().add(new VBox(2, heading, MissionSupportDialog.paragraph(note.text())));
        }
        if (journal.size() < 2) {
            entries.getChildren().add(MissionSupportDialog.paragraph("สำรวจจุดเอกสารสีทองในโถงเพื่ออ่านข้อมูลที่ยังขาด"));
        }
        TitledPane pane = new TitledPane("สมุดภาคสนาม  " + journal.size() + "/2", entries);
        pane.getStyleClass().add("field-journal");
        pane.setAnimated(false);
        pane.setExpanded(!journal.isEmpty());
        return pane;
    }

    private static VBox deskPanel(FieldActivity.RecordDesk desk, Button confirm, Dialog<Void> dialog,
                                  Predicate<FieldActivity.Attempt> submit, Label feedback) {
        List<Integer> selected = new ArrayList<>();
        var buttons = new ArrayList<Button>();
        VBox rows = new VBox(5);
        int required = desk.target().size();
        Label status = MissionSupportDialog.paragraph("");
        confirm.setId("confirmRecords");
        confirm.setText("ยืนยันรายการ");
        for (int index = 0; index < desk.rows().size(); index++) {
            int row = index;
            Button choice = action("", "recordRow" + index, "field-record");
            choice.setMaxWidth(Double.MAX_VALUE);
            choice.setWrapText(true);
            choice.setOnAction(event -> {
                if (selected.contains(row)) {
                    selected.remove(Integer.valueOf(row));
                } else if (selected.size() < required) {
                    selected.add(row);
                }
                feedback.setText("");
                showDeskSelection(desk, selected, buttons, status, confirm);
            });
            buttons.add(choice);
            rows.getChildren().add(choice);
        }
        showDeskSelection(desk, selected, buttons, status, confirm);
        confirm.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            verify(dialog, submit, new FieldActivity.Attempt.Records(selected), feedback,
                    "รายการยังไม่ตรงกัน ลองเทียบข้อมูลจากเอกสารทั้งสองฉบับอีกครั้ง");
        });
        return new VBox(9, status, rows);
    }

    private static VBox signalPanel(FieldActivity.Signal signal, Button confirm, Dialog<Void> dialog,
                                    Predicate<FieldActivity.Attempt> submit, Label feedback) {
        Canvas display = new Canvas(470, 128);
        Slider frequency = dial("signalFrequency", "ความถี่", 1, 6);
        Slider phase = dial("signalPhase", "เฟส", 0, 7);
        Label state = MissionSupportDialog.paragraph("");
        confirm.setId("confirmSignal");
        confirm.setText("บันทึกสัญญาณ");
        Runnable update = () -> {
            var attempt = new FieldActivity.Attempt.Tune((int) frequency.getValue(), (int) phase.getValue());
            var graphics = display.getGraphicsContext2D();
            graphics.setFill(Color.web("#111a2d"));
            graphics.fillRect(0, 0, display.getWidth(), display.getHeight());
            graphics.setStroke(Color.web("#324664"));
            graphics.strokeLine(0, 64, display.getWidth(), 64);
            drawWave(display, signal, signal.frequency(), signal.phase(), Color.web("#ffcc80"), 4);
            drawWave(display, signal, attempt.frequency(), attempt.phase(), Color.web("#83e9ff"), 2);
            boolean matches = signal.solvedBy(attempt);
            state.setText(matches ? "จับสัญญาณได้แล้ว" : "สัญญาณยังไม่ตรง");
            confirm.setDisable(!matches);
        };
        frequency.valueProperty().addListener((property, before, after) -> update.run());
        phase.valueProperty().addListener((property, before, after) -> update.run());
        confirm.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            verify(dialog, submit, new FieldActivity.Attempt.Tune((int) frequency.getValue(), (int) phase.getValue()),
                    feedback, "สัญญาณยังไม่ตรง");
        });
        update.run();
        return new VBox(6, display, MissionSupportDialog.paragraph("ความถี่"), frequency,
                MissionSupportDialog.paragraph("เฟส"), phase, state);
    }

    private static Slider dial(String id, String title, int min, int max) {
        Slider slider = new Slider(min, max, min);
        slider.setId(id);
        slider.setAccessibleText(title);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        return slider;
    }

    private static void drawWave(Canvas display, FieldActivity.Signal signal, int frequency, int phase,
                                 Color color, int width) {
        var graphics = display.getGraphicsContext2D();
        graphics.setStroke(color);
        graphics.setLineWidth(width);
        graphics.beginPath();
        graphics.moveTo(0, 64 - 42 * signal.sample(0, frequency, phase));
        for (int x = 1; x < display.getWidth(); x++) {
            graphics.lineTo(x, 64 - 42 * signal.sample(x / display.getWidth(), frequency, phase));
        }
        graphics.stroke();
    }

    private static VBox circuitPanel(FieldActivity.Circuit circuit, Button confirm, Dialog<Void> dialog,
                                     Predicate<FieldActivity.Attempt> submit, Label feedback) {
        GridPane board = new GridPane(4, 4);
        board.add(MissionSupportDialog.paragraph("IN →"), 0, 1);
        board.add(MissionSupportDialog.paragraph("→ OUT"), 5, 2);
        var turns = new ArrayList<>(Collections.nCopies(circuit.tiles().size(), 0));
        var displays = new ArrayList<Canvas>();
        Label state = MissionSupportDialog.paragraph("");
        confirm.setId("confirmCircuit");
        confirm.setText("จ่ายไฟเข้าเครื่องอ่าน");
        Runnable update = () -> {
            var powered = circuit.powered(turns);
            for (int tile = 0; tile < displays.size(); tile++) {
                drawConnector(displays.get(tile), circuit.connector(tile, turns.get(tile)), powered.contains(tile));
            }
            boolean linked = circuit.solvedBy(new FieldActivity.Attempt.Wire(turns));
            state.setText(linked ? "ไฟถึงจุด OUT แล้ว" : "วงจรยังไม่ถึงจุด OUT");
            confirm.setDisable(!linked);
        };
        for (int index = 0; index < circuit.tiles().size(); index++) {
            int tile = index;
            Canvas graphic = new Canvas(37, 37);
            displays.add(graphic);
            Button button = action("", "wireTile" + index, "field-cell");
            button.setGraphic(graphic);
            button.setAccessibleText("หมุนแผ่น " + (index + 1));
            button.setDisable(circuit.tiles().get(index) == 0);
            button.setOnAction(event -> {
                turns.set(tile, (turns.get(tile) + 1) % 4);
                update.run();
            });
            board.add(button, index % 4 + 1, index / 4);
        }
        confirm.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            verify(dialog, submit, new FieldActivity.Attempt.Wire(turns), feedback, "วงจรยังไม่ครบ");
        });
        update.run();
        return new VBox(8, board, state);
    }

    private static void drawConnector(Canvas graphic, int mask, boolean powered) {
        var graphics = graphic.getGraphicsContext2D();
        graphics.clearRect(0, 0, 37, 37);
        graphics.setStroke(powered ? Color.web("#b0f490") : Color.web("#d39461"));
        graphics.setLineWidth(4);
        int[] x = {18, 37, 18, 0};
        int[] y = {0, 18, 37, 18};
        for (int direction = 0; direction < 4; direction++) {
            if ((mask & (1 << direction)) != 0) {
                graphics.strokeLine(18, 18, x[direction], y[direction]);
            }
        }
    }

    private static void showDeskSelection(FieldActivity.RecordDesk desk, List<Integer> selected,
                                          List<Button> buttons, Label status, Button confirm) {
        for (int index = 0; index < buttons.size(); index++) {
            Button button = buttons.get(index);
            int position = selected.indexOf(index);
            String mark = position < 0 ? "○" : desk.ordered() ? Integer.toString(position + 1) : "✓";
            button.pseudoClassStateChanged(MARKED, position >= 0);
            button.setText(ThaiTypography.withWordBreaks(mark + "   " + desk.rows().get(index)));
        }
        status.setText("เลือกแล้ว " + selected.size() + " / " + desk.target().size());
        confirm.setDisable(selected.size() != desk.target().size());
    }

    private static Button action(String title, String id, String style) {
        Button button = new Button(title);
        button.setId(id);
        button.getStyleClass().add(style);
        return button;
    }

    private static void verify(Dialog<Void> dialog, Predicate<FieldActivity.Attempt> submit,
                               FieldActivity.Attempt attempt, Label status, String retry) {
        if (submit.test(attempt)) {
            dialog.close();
        } else {
            status.setText(ThaiTypography.withWordBreaks(retry));
        }
    }
}
