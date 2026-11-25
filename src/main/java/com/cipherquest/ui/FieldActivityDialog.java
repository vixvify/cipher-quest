package com.cipherquest.ui;

import com.cipherquest.exploration.FieldActivity;
import com.cipherquest.exploration.FieldNote;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
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
        String task = switch (note.activity()) {
            case FieldActivity.RecordDesk desk -> desk.task();
            case FieldActivity.SurveyRoute route -> route.task();
            case FieldActivity.Source ignored -> throw new IllegalArgumentException("Display sources directly");
        };
        Dialog<Void> dialog = MissionSupportDialog.createDialog(owner, note.title(), "");
        dialog.getDialogPane().setPrefWidth(605);
        VBox controls = switch (note.activity()) {
            case FieldActivity.RecordDesk desk -> deskPanel(desk, dialog, submit);
            case FieldActivity.SurveyRoute route -> routePanel(route, dialog, submit);
            case FieldActivity.Source ignored -> throw new IllegalArgumentException("Display sources directly");
        };
        VBox content = new VBox(12, MissionSupportDialog.paragraph(task), journalPane(journal), controls);
        content.setPadding(new Insets(0, 7, 6, 0));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(380);
        dialog.getDialogPane().setContent(scroll);
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
        pane.setExpanded(false);
        return pane;
    }

    private static VBox deskPanel(FieldActivity.RecordDesk desk, Dialog<Void> dialog,
                                  Predicate<FieldActivity.Attempt> submit) {
        List<Integer> selected = new ArrayList<>();
        var buttons = new ArrayList<Button>();
        VBox rows = new VBox(5);
        int required = desk.target().size();
        Label status = MissionSupportDialog.paragraph("");
        Label feedback = MissionSupportDialog.paragraph("");
        Button confirm = action("ยืนยันรายการ", "confirmRecords", "primary-button");
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
        confirm.setOnAction(event -> verify(dialog, submit, new FieldActivity.Attempt.Records(selected), feedback,
                "รายการยังไม่ตรงกัน ลองเทียบข้อมูลจากเอกสารทั้งสองฉบับอีกครั้ง"));
        return new VBox(9, status, rows, confirm, feedback);
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

    private static VBox routePanel(FieldActivity.SurveyRoute route, Dialog<Void> dialog,
                                   Predicate<FieldActivity.Attempt> submit) {
        List<Integer> path = new ArrayList<>(List.of(route.start()));
        List<Button> cells = new ArrayList<>();
        GridPane map = new GridPane(5, 5);
        Label status = MissionSupportDialog.paragraph("");
        Label feedback = MissionSupportDialog.paragraph("");
        Button undo = action("ย้อนหนึ่งช่อง", "undoRoute", "secondary-button");
        Button reset = action("เริ่มทางใหม่", "resetRoute", "secondary-button");
        Button confirm = action("ส่งเส้นทาง", "confirmRoute", "primary-button");
        for (int index = 0; index < route.cellCount(); index++) {
            int cell = index;
            Button button = action("", "routeCell" + index, "field-cell");
            button.setMinSize(52, 35);
            button.setOnAction(event -> {
                if (route.canStep(path, cell)) {
                    path.add(cell);
                }
                showRoute(route, path, cells, status, undo, confirm);
                feedback.setText("");
            });
            cells.add(button);
            map.add(button, index % route.columns(), index / route.columns());
        }
        undo.setOnAction(event -> {
            path.removeLast();
            showRoute(route, path, cells, status, undo, confirm);
            feedback.setText("");
        });
        reset.setOnAction(event -> {
            path.clear();
            path.add(route.start());
            showRoute(route, path, cells, status, undo, confirm);
            feedback.setText("");
        });
        confirm.setOnAction(event -> verify(dialog, submit, new FieldActivity.Attempt.Route(path), feedback,
                "เส้นทางยังไม่ผ่านการตรวจ ลองดูจุดที่ต้องแวะ ลำดับ และทางปิดในสมุดภาคสนาม"));
        showRoute(route, path, cells, status, undo, confirm);
        Label legend = MissionSupportDialog.paragraph(route.names().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "  " + entry.getValue()).collect(java.util.stream.Collectors.joining("   ·   ")));
        return new VBox(10, status, map, legend, new HBox(7, confirm, undo, reset), feedback);
    }

    private static void showRoute(FieldActivity.SurveyRoute route, List<Integer> path, List<Button> cells,
                                  Label status, Button undo, Button confirm) {
        for (int index = 0; index < cells.size(); index++) {
            Button button = cells.get(index);
            char marker = route.markerAt(index);
            int step = path.indexOf(index);
            String name = route.names().getOrDefault(marker, marker == '#' ? "กำแพง" : "ทางเดิน");
            String symbol = marker == '.' ? "·" : Character.toString(marker);
            button.setText(step >= 0 ? (marker == '.' ? Integer.toString(step) : symbol + " " + step) : symbol);
            button.setAccessibleText(name + (step >= 0 ? " ลำดับ " + step : ""));
            button.pseudoClassStateChanged(MARKED, step >= 0);
            button.setDisable(step < 0 && !route.canStep(path, index));
        }
        status.setText("ช่องที่เดิน " + (path.size() - 1) + " / " + route.stepLimit());
        undo.setDisable(path.size() == 1);
        confirm.setDisable(route.markerAt(path.getLast()) != 'E');
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
