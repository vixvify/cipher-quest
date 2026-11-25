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
        if (!(note.activity() instanceof FieldActivity.RecordDesk desk)) {
            throw new IllegalArgumentException("Only record desks open a selection dialog");
        }
        Dialog<Void> dialog = MissionSupportDialog.createDialog(owner, note.title(), "");
        dialog.getDialogPane().setPrefWidth(605);
        VBox controls = deskPanel(desk, dialog, submit);
        VBox content = new VBox(12, MissionSupportDialog.paragraph(desk.task()), journalPane(journal), controls);
        content.setPadding(new Insets(0, 7, 6, 0));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(460);
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
        pane.setExpanded(!journal.isEmpty());
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
        return new VBox(9, new HBox(10, status, confirm), rows, feedback);
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
