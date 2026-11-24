package com.cipherquest.ui;

import com.cipherquest.exploration.FieldActivity;
import com.cipherquest.exploration.FieldNote;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class FieldActivityDialog {
    private FieldActivityDialog() {}

    static void show(Node owner, FieldNote note, Predicate<FieldActivity.Attempt> submit, Runnable onClose) {
        Dialog<Void> dialog = MissionSupportDialog.createDialog(owner, note.title(), instructions(note.activity()));
        VBox content = switch (note.activity()) {
            case FieldActivity.Radio radio -> radioPanel(radio, dialog, submit);
            case FieldActivity.Sequence sequence -> sequencePanel(sequence, dialog, submit);
            case FieldActivity.Match match -> matchPanel(match, dialog, submit);
            case FieldActivity.Circuit circuit -> circuitPanel(circuit, dialog, submit);
            case FieldActivity.Pickup ignored -> throw new IllegalArgumentException("Pickup opens directly");
        };
        dialog.getDialogPane().setContent(content);
        dialog.setOnHidden(event -> onClose.run());
        dialog.show();
    }

    private static String instructions(FieldActivity activity) {
        return switch (activity) {
            case FieldActivity.Radio ignored -> "เลื่อนหน้าปัดจนแถบสัญญาณเต็ม แล้วกดยืนยันคลื่น";
            case FieldActivity.Sequence ignored -> "เลือกเศษกระดาษจากซ้ายไปขวา ให้รอยขอบด้านขวาต่อกับรอยด้านซ้ายของชิ้นถัดไป";
            case FieldActivity.Match match -> match.prompt();
            case FieldActivity.Circuit ignored -> "เปิดไฟให้ครบ 9 ช่อง แต่ละสวิตช์จะสลับไฟช่องของตัวเองและช่องที่ติดกันด้านบน ล่าง ซ้าย ขวา";
            case FieldActivity.Pickup ignored -> "";
        };
    }

    private static VBox radioPanel(FieldActivity.Radio radio, Dialog<Void> dialog,
                                  Predicate<FieldActivity.Attempt> submit) {
        Slider dial = new Slider(FieldActivity.Radio.MIN_FREQUENCY, FieldActivity.Radio.MAX_FREQUENCY,
                FieldActivity.Radio.MIN_FREQUENCY);
        dial.setId("radioDial");
        dial.setAccessibleText("ความถี่วิทยุ");
        dial.setBlockIncrement(1);
        dial.setMajorTickUnit(5);
        dial.setMinorTickCount(4);
        dial.setSnapToTicks(true);
        dial.setShowTickLabels(true);
        dial.setShowTickMarks(true);
        ProgressBar signal = new ProgressBar();
        signal.setMaxWidth(Double.MAX_VALUE);
        signal.setAccessibleText("ความแรงสัญญาณ");
        Label reading = MissionSupportDialog.paragraph("");
        dial.valueProperty().addListener((property, before, now) ->
                showSignal(radio, channel(dial), signal, reading));
        showSignal(radio, channel(dial), signal, reading);
        Label status = MissionSupportDialog.paragraph("");
        Button confirm = action("ยืนยันคลื่น", "confirmFrequency", "primary-button");
        confirm.setOnAction(event -> verify(dialog, submit, new FieldActivity.Attempt.Tune(channel(dial)),
                status, "สัญญาณยังไม่ตรง ลองปรับหน้าปัดอีกครั้ง"));
        return new VBox(12, reading, dial, signal, confirm, status);
    }

    private static int channel(Slider dial) {
        return (int) Math.round(dial.getValue());
    }

    private static void showSignal(FieldActivity.Radio radio, int channel, ProgressBar signal, Label reading) {
        double strength = radio.signalStrength(channel);
        signal.setProgress(strength);
        reading.setText(channel + " MHz  ·  สัญญาณ " + Math.round(strength * 100) + "%");
    }

    private static VBox sequencePanel(FieldActivity.Sequence sequence, Dialog<Void> dialog,
                                     Predicate<FieldActivity.Attempt> submit) {
        List<Integer> selected = new ArrayList<>();
        VBox options = new VBox(6);
        VBox arrangement = new VBox(6);
        Label status = MissionSupportDialog.paragraph("");
        Button confirm = action("ตรวจลำดับ", "confirmSequence", "primary-button");
        Button reset = action("เริ่มเรียงใหม่", "resetSequence", "secondary-button");
        confirm.setDisable(true);
        for (int i = 0; i < sequence.pieces().size(); i++) {
            int index = i;
            String piece = sequence.pieces().get(index);
            Button choice = action(ThaiTypography.withWordBreaks(piece), "sequencePiece" + index, "support-button");
            choice.setMaxWidth(Double.MAX_VALUE);
            choice.setOnAction(event -> {
                choice.setDisable(true);
                selected.add(index);
                arrangement.getChildren().add(MissionSupportDialog.paragraph(selected.size() + ".  " + piece));
                status.setText("");
                confirm.setDisable(selected.size() != sequence.pieces().size());
            });
            options.getChildren().add(choice);
        }
        reset.setOnAction(event -> {
            selected.clear();
            arrangement.getChildren().clear();
            options.getChildren().forEach(option -> option.setDisable(false));
            confirm.setDisable(true);
            status.setText("");
        });
        confirm.setOnAction(event -> verify(dialog, submit, new FieldActivity.Attempt.Assemble(selected),
                status, "รอยขอบยังต่อกันไม่ครบ ลองเรียงใหม่"));
        return new VBox(9, options, arrangement, new HBox(8, confirm, reset), status);
    }

    private static VBox matchPanel(FieldActivity.Match match, Dialog<Void> dialog,
                                   Predicate<FieldActivity.Attempt> submit) {
        Label status = MissionSupportDialog.paragraph("");
        VBox options = new VBox(8);
        for (int i = 0; i < match.options().size(); i++) {
            int index = i;
            Button entry = action(match.options().get(i), "matchOption" + index, "support-button");
            entry.setMaxWidth(Double.MAX_VALUE);
            entry.setOnAction(event -> verify(dialog, submit, new FieldActivity.Attempt.MatchMark(index),
                    status, "ตรายังไม่ตรง ลองเทียบรูปกับจำนวนรอยบากอีกครั้ง"));
            options.getChildren().add(entry);
        }
        return new VBox(12, options, status);
    }

    private static VBox circuitPanel(FieldActivity.Circuit circuit, Dialog<Void> dialog,
                                     Predicate<FieldActivity.Attempt> submit) {
        List<Integer> pressed = new ArrayList<>();
        List<Button> tiles = new ArrayList<>();
        GridPane grid = new GridPane(7, 7);
        Label status = MissionSupportDialog.paragraph("");
        for (int tile = 0; tile < FieldActivity.Circuit.SIZE; tile++) {
            int index = tile;
            Button tileButton = action("", "circuitTile" + tile, "circuit-light");
            tileButton.setMinSize(64, 43);
            tileButton.setOnAction(event -> {
                pressed.add(index);
                showLights(circuit.lightsAfter(pressed), tiles, status);
                if (submit.test(new FieldActivity.Attempt.Switches(pressed))) {
                    dialog.close();
                }
            });
            tiles.add(tileButton);
            grid.add(tileButton, tile % FieldActivity.Circuit.SIDE, tile / FieldActivity.Circuit.SIDE);
        }
        Button reset = action("รีเซ็ตแผง", "resetCircuit", "secondary-button");
        reset.setOnAction(event -> {
            pressed.clear();
            showLights(circuit.lightsAfter(pressed), tiles, status);
        });
        showLights(circuit.lightsAfter(pressed), tiles, status);
        return new VBox(12, status, grid, reset);
    }

    private static void showLights(int lights, List<Button> tiles, Label status) {
        for (int index = 0; index < tiles.size(); index++) {
            Button tile = tiles.get(index);
            boolean lit = (lights & (1 << index)) != 0;
            tile.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("lit"), lit);
            tile.setText(lit ? "เปิด" : "ปิด");
            tile.setAccessibleText("ช่อง " + (index + 1) + " ไฟ" + (lit ? "เปิด" : "ปิด"));
        }
        status.setText("ไฟติด " + Integer.bitCount(lights) + " / " + tiles.size());
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
