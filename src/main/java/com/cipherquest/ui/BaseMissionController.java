package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.exploration.FieldNote;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public abstract class BaseMissionController {
    private static final PseudoClass CLEARED = PseudoClass.getPseudoClass("cleared");

    @FXML protected StackPane missionRoot;
    @FXML protected Button hintButton;
    @FXML protected Button backToArchiveButton;
    @FXML protected Button fieldNotesButton;
    @FXML protected Label checkpointLabel;
    @FXML protected Label missionTitle;
    @FXML protected Label missionLabel;

    protected void initializeMissionHeader() {
        ScreenNavigator.SESSION.currentMission().ifPresent(mission -> {
            if (checkpointLabel != null) {
                checkpointLabel.setText(ScreenNavigator.checkpointText(mission));
            }
            if (missionTitle != null) {
                missionTitle.setText(ThaiTypography.withWordBreaks(mission.title()));
            }
            if (missionLabel != null) {
                missionLabel.setText(ThaiTypography.withWordBreaks(mission.description()));
            }
        });
        boolean canReturn = ScreenNavigator.archiveWalk() != null;
        backToArchiveButton.setVisible(canReturn);
        backToArchiveButton.setManaged(canReturn);
        backToArchiveButton.setTooltip(new Tooltip("กลับโถงสำรวจ (Esc)"));
        if (fieldNotesButton != null) {
            fieldNotesButton.setVisible(canReturn);
            fieldNotesButton.setManaged(canReturn);
            fieldNotesButton.setText("หลักฐาน " + currentFieldNotes().size());
        }
        missionRoot.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (canReturn && event.getCode() == KeyCode.ESCAPE && !backToArchiveButton.isDisabled()) {
                event.consume();
                onReturnToArchive();
            }
        });
        setHintsBusy(false);
    }

    @FXML
    protected void onOpenHints() {
        MissionSupportDialog.showHints(hintButton, ScreenNavigator.SESSION);
    }

    @FXML
    protected void onOpenFieldNotes() {
        MissionSupportDialog.showFieldNotes(fieldNotesButton, currentFieldNotes());
    }

    private List<FieldNote> currentFieldNotes() {
        var walk = ScreenNavigator.archiveWalk();
        if (walk == null) {
            return List.of();
        }
        return ScreenNavigator.SESSION.currentMission()
                .map(mission -> walk.collectedNotes(mission.checkpoint()))
                .orElseGet(List::of);
    }

    @FXML
    protected void onReturnToArchive() {
        ScreenNavigator.navigateToArchive();
    }

    protected void setHintsBusy(boolean busy) {
        hintButton.setDisable(busy || ScreenNavigator.SESSION.hintProgress().total() == 0);
        backToArchiveButton.setDisable(busy);
        if (fieldNotesButton != null) {
            fieldNotesButton.setDisable(busy);
        }
    }

    protected void proceedAfterFeedback(Node node, AnswerResult result, Duration delay) {
        setHintsBusy(true);
        GameAudio.play(GameAudio.Effect.CLEAR);
        showClearBanner(result);
        ScreenNavigator.continueAfterFeedback(node, result, delay);
    }

    private void showClearBanner(AnswerResult result) {
        Label title = bannerText("ผ่านด่านแล้ว", "clear-title");
        boolean finalCheckpoint = ScreenNavigator.SESSION.currentMission()
                .map(mission -> mission.checkpoint() == ScreenNavigator.SESSION.campaignProgress().checkpoints().size())
                .orElse(false);
        Label detail = bannerText(finalCheckpoint ? "ภารกิจเสร็จสิ้น" : "ปลดล็อกสถานีถัดไป", "clear-detail");
        VBox message = new VBox(2, title, detail);
        message.setMinWidth(0);
        HBox.setHgrow(message, Priority.ALWAYS);
        Label score = bannerText("+" + result.getPointsAwarded(), "clear-score");
        HBox banner = new HBox(18, message, score);
        banner.setId("missionClearBanner");
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setMaxSize(460, HBox.USE_PREF_SIZE);
        banner.getStyleClass().add("clear-banner");
        banner.setMouseTransparent(true);
        StackPane.setAlignment(banner, Pos.BOTTOM_CENTER);
        StackPane.setMargin(banner, new Insets(0, 0, 10, 0));
        missionRoot.getChildren().add(banner);
        missionRoot.pseudoClassStateChanged(CLEARED, true);

        if (!Platform.getPreferences().isReducedMotion()) {
            Duration duration = Duration.millis(300);
            FadeTransition fade = new FadeTransition(duration, banner);
            fade.setFromValue(0);
            fade.setToValue(1);
            TranslateTransition rise = new TranslateTransition(duration, banner);
            rise.setFromY(22);
            rise.setToY(0);
            rise.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fade, rise).play();
        }
    }

    private static Label bannerText(String text, String style) {
        Label label = new Label(ThaiTypography.withWordBreaks(text));
        label.getStyleClass().add(style);
        label.setWrapText(true);
        return label;
    }
}
