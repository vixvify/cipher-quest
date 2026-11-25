package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PasskeyScreenController extends BaseMissionController {
    private static final PseudoClass REVEALED = PseudoClass.getPseudoClass("revealed");
    private static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");
    private String ciphertext;
    private boolean evidenceRevealed;
    private boolean flipping;

    @FXML private PasswordField passkeyField;
    @FXML private Button authenticateButton;
    @FXML private Button evidenceButton;
    @FXML private Label feedbackLabel;
    @FXML private Label evidenceCaption;
    @FXML private Label evidenceText;
    @FXML private StackPane localEvidence;
    @FXML private VBox carriedEvidence;
    @FXML private Label carriedCiphertext;
    @FXML private Label missingEvidence;
    @FXML private HBox keySlots;

    @FXML
    private void initialize() {
        initializeMissionHeader();
        ScreenNavigator.SESSION.passkeyBrief().ifPresent(brief -> ciphertext = brief.ciphertext());
        if (ciphertext != null) {
            for (int index = 0; index < ciphertext.length(); index++) {
                Region slot = new Region();
                slot.getStyleClass().add("passkey-slot");
                keySlots.getChildren().add(slot);
            }
        }
        passkeyField.textProperty().addListener((property, before, value) -> {
            int entered = value.codePointCount(0, value.length());
            for (int index = 0; index < keySlots.getChildren().size(); index++) {
                keySlots.getChildren().get(index).pseudoClassStateChanged(FILLED, index < entered);
            }
        });
        evidenceButton.setDisable(ciphertext == null);
        var walk = ScreenNavigator.archiveWalk();
        boolean inExploration = walk != null;
        localEvidence.setVisible(!inExploration);
        localEvidence.setManaged(!inExploration);
        boolean hasCarriedEvidence = inExploration && walk.hasRecoveredClue(1) && ciphertext != null;
        carriedEvidence.setVisible(hasCarriedEvidence);
        carriedEvidence.setManaged(hasCarriedEvidence);
        missingEvidence.setVisible(inExploration && !hasCarriedEvidence);
        missingEvidence.setManaged(inExploration && !hasCarriedEvidence);
        if (hasCarriedEvidence) {
            carriedCiphertext.setText(ciphertext);
        }
    }

    @FXML
    private void onFlipEvidence() {
        if (flipping) {
            return;
        }
        if (Platform.getPreferences().isReducedMotion()) {
            showOtherSide();
            return;
        }

        flipping = true;
        ScaleTransition close = new ScaleTransition(Duration.millis(120), evidenceButton);
        close.setToX(0.06);
        close.setOnFinished(event -> {
            showOtherSide();
            ScaleTransition open = new ScaleTransition(Duration.millis(155), evidenceButton);
            open.setToX(1);
            open.setOnFinished(finished -> flipping = false);
            open.play();
        });
        close.play();
    }

    private void showOtherSide() {
        evidenceRevealed = !evidenceRevealed;
        evidenceButton.pseudoClassStateChanged(REVEALED, evidenceRevealed);
        evidenceCaption.setText(evidenceRevealed ? "CIPHERTEXT" : "INTERCEPTED");
        evidenceText.setText(evidenceRevealed ? ciphertext : "พลิกดูด้านหลัง  ↻");
        evidenceButton.setAccessibleText(evidenceRevealed ? "ข้อความเข้ารหัส " + ciphertext : "พลิกบัตรหลักฐาน");
    }

    @FXML
    private void onSubmitPasskey() {
        String input = passkeyField.getText();
        if (input.isBlank()) {
            Feedback.show(feedbackLabel, "กรุณากรอก Passkey ก่อนกดยืนยัน", false, true);
            return;
        }

        AnswerResult result = ScreenNavigator.SESSION.submit(input);
        Feedback.show(feedbackLabel, result.getMessage(), result.isCorrect(), !result.isCorrect());
        if (result.isCorrect()) {
            passkeyField.setDisable(true);
            authenticateButton.setDisable(true);
            proceedAfterFeedback(passkeyField, result, Duration.seconds(1.2));
        } else {
            GameAudio.play(GameAudio.Effect.DENIED);
            passkeyField.clear();
            passkeyField.requestFocus();
        }
    }
}
