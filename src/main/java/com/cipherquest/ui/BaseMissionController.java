package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public abstract class BaseMissionController {
    private static final PseudoClass CLEARED = PseudoClass.getPseudoClass("cleared");

    @FXML protected StackPane missionRoot;
    @FXML protected Button hintButton;
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
        setHintsBusy(false);
    }

    @FXML
    protected void onOpenHints() {
        MissionSupportDialog.showHints(hintButton, ScreenNavigator.SESSION);
    }

    protected void setHintsBusy(boolean busy) {
        hintButton.setDisable(busy || ScreenNavigator.SESSION.hintProgress().total() == 0);
    }

    protected void proceedAfterFeedback(Node node, AnswerResult result, Duration delay) {
        setHintsBusy(true);
        showClearBanner(result);
        ScreenNavigator.continueAfterFeedback(node, result, delay);
    }

    private void showClearBanner(AnswerResult result) {
        Label title = bannerText("ผ่านด่านแล้ว", "clear-title");
        Label detail = ScreenNavigator.SESSION.currentMissionClue()
                .map(clue -> bannerText(clue.getTitle(), "clear-detail"))
                .orElseGet(() -> bannerText("ปลดล็อกด่านถัดไป", "clear-detail"));
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
