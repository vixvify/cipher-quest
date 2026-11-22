package com.cipherquest.ui;

import com.cipherquest.clues.Clue;
import com.cipherquest.core.GameSession;
import com.cipherquest.knowledge.SqlTopic;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;

final class MissionSupportDialog {
    private MissionSupportDialog() {}

    static void showHints(Node owner, GameSession session) {
        Dialog<Void> dialog = createDialog(owner, "คำใบ้ภารกิจ",
                "เปิดทีละขั้นเมื่อคิดต่อไม่ออก การเปิดคำใบ้มีผลต่อคะแนนด่าน");
        VBox history = new VBox(10);
        Button reveal = new Button();
        reveal.setId("revealHintButton");
        reveal.getStyleClass().add("primary-button");
        ScrollPane scroll = scrollable(history);
        scroll.setPrefViewportHeight(170);
        setHintProgress(history, reveal, session.hintProgress());
        reveal.setOnAction(event -> {
            setHintProgress(history, reveal, session.revealHint());
            Platform.runLater(() -> scroll.setVvalue(1));
        });
        dialog.getDialogPane().setContent(new VBox(12, scroll, reveal));
        dialog.show();
    }

    static void showKnowledge(Node owner, List<SqlTopic> topics) {
        Dialog<Void> dialog = createDialog(owner, "ความรู้ SQL สำหรับด่านนี้",
                "เปิดอ่านระหว่างเขียน Query ได้โดยไม่เสียคะแนน ตัวอย่างใช้ตารางสมมติ");
        chooseEntry(dialog, topics, 0, SqlTopic::getTitle, topic -> new VBox(10,
                paragraph(topic.getConceptOverview()),
                section("รูปแบบคำสั่ง", topic.getSyntax(), "support-code"),
                paragraph(topic.getExplanation()),
                section("ตัวอย่าง", topic.getExampleQuery(), "support-code"),
                section("ผลลัพธ์ที่คาดไว้", topic.getExpectedResult(), "support-copy"),
                section("จุดที่มักพลาด", topic.getCommonMistakes(), "support-copy")));
        dialog.show();
    }

    static void showClues(Node owner, List<Clue> clues) {
        Dialog<Void> dialog = createDialog(owner, "คลังเบาะแส",
                "ย้อนดูรหัส ชื่อสถานที่ และข้อมูลจากด่านก่อนหน้าเพื่อนำไปใช้ใน Query");
        chooseEntry(dialog, clues, clues.size() - 1, Clue::getTitle, clue -> new VBox(10,
                paragraph(clue.getSummary()),
                section("ข้อมูลที่ค้นพบ", String.join("\n", clue.getDetails()), "support-copy")));
        dialog.show();
    }

    private static void setHintProgress(VBox history, Button reveal, GameSession.HintProgress progress) {
        history.getChildren().clear();
        List<String> revealed = progress.revealed();
        if (revealed.isEmpty()) {
            history.getChildren().add(paragraph("ยังไม่ได้เปิดคำใบ้"));
        }
        for (int index = 0; index < revealed.size(); index++) {
            history.getChildren().add(section("คำใบ้ " + (index + 1), revealed.get(index), "support-copy"));
        }
        reveal.setDisable(!progress.canReveal());
        reveal.setText(progress.canReveal()
                ? "เปิดคำใบ้ " + (revealed.size() + 1) + " / " + progress.total()
                : "เปิดคำใบ้ครบแล้ว");
    }

    private static <T> void chooseEntry(Dialog<Void> dialog, List<T> entries, int initialIndex,
                                        Function<T, String> title, Function<T, Node> content) {
        if (entries.isEmpty()) {
            dialog.getDialogPane().setContent(paragraph("ยังไม่มีข้อมูลสำหรับด่านนี้"));
            return;
        }
        ComboBox<String> picker = new ComboBox<>();
        picker.getItems().setAll(entries.stream().map(title).toList());
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setAccessibleText("เลือกหัวข้อ");
        VBox details = new VBox();
        ScrollPane scroll = scrollable(details);
        picker.getSelectionModel().selectedIndexProperty().addListener((property, previous, selected) -> {
            if (selected.intValue() >= 0) {
                details.getChildren().setAll(content.apply(entries.get(selected.intValue())));
                scroll.setVvalue(0);
            }
        });
        picker.getSelectionModel().select(initialIndex);
        dialog.getDialogPane().setContent(new VBox(12, picker, scroll));
    }

    private static Dialog<Void> createDialog(Node owner, String title, String instructions) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner.getScene().getWindow());
        dialog.setTitle(title);
        var pane = dialog.getDialogPane();
        pane.getStyleClass().add("mission-support");
        pane.getStylesheets().add(MissionSupportDialog.class.getResource("/css/monitor-shared.css").toExternalForm());
        pane.setPrefWidth(520);
        pane.setHeader(new VBox(6, label(title, "support-title"), paragraph(instructions)));
        pane.getButtonTypes().add(new ButtonType("ปิด", ButtonBar.ButtonData.CANCEL_CLOSE));
        return dialog;
    }

    private static ScrollPane scrollable(Node content) {
        content.getStyleClass().add("support-scroll-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(230);
        return scroll;
    }

    private static Node section(String title, String body, String style) {
        return new VBox(3, label(title, "support-section-title"), label(body, style));
    }

    private static Label paragraph(String text) {
        return label(text, "support-copy");
    }

    private static Label label(String text, String style) {
        Label label = new Label(ThaiTypography.withWordBreaks(text));
        label.getStyleClass().add(style);
        label.setWrapText(true);
        label.setMinHeight(Label.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
