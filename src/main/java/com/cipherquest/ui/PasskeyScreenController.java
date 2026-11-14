package com.cipherquest.ui;

import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameEngine;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.caesar.CaesarLevel;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้า Passkey Screen (ด่าน 1 — Caesar Cipher)
 *
 * แสดงการแยกความรับผิดชอบอย่างชัดเจน (Separation of Concerns):
 * Controller ทำหน้าที่แสดงผล UI และรับ Input จากผู้เล่นเท่านั้น
 * เมื่อผู้เล่นกดส่งคำตอบ จะส่งผ่านไปให้ GameEngine ตรวจสอบ (ไม่ตรวจเองที่ Controller)
 */
public class PasskeyScreenController implements Initializable {

    @FXML private PasswordField passkeyField;
    @FXML private Label feedbackLabel;
    @FXML private Label hintLabel;
    @FXML private Label ciphertextLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Level currentLevel = GameEngine.getInstance().getCurrentLevel();
        if (currentLevel != null) {
            hintLabel.setText(currentLevel.getHint());
            if (currentLevel instanceof CaesarLevel caesar) {
                ciphertextLabel.setText(caesar.getCiphertext());
            }
        }
        feedbackLabel.setText("");
    }

    @FXML
    private void onSubmitPasskey() {
        String input = passkeyField.getText();
        if (input == null || input.trim().isEmpty()) {
            feedbackLabel.setText("กรุณากรอก Passkey ก่อนกดยืนยัน");
            return;
        }

        // ส่ง input ให้ Core GameEngine เป็นผู้ประเมินคำตอบ
        AnswerResult result = GameEngine.getInstance().submitAnswer(input);

        if (result.isCorrect()) {
            feedbackLabel.setStyle("-fx-text-fill: #3ec97e;");
            feedbackLabel.setText("✔ " + result.getMessage());

            // หน่วงเวลาเล็กน้อยเพื่อให้ผู้เล่นเห็นข้อความสำเร็จ ก่อนเปลี่ยนด่าน
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
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
            feedbackLabel.setStyle("-fx-text-fill: #e05252;");
            feedbackLabel.setText("✖ " + result.getMessage());
            passkeyField.clear();

            if (result.getStatus() == AnswerResult.Status.GAME_OVER) {
                PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
                pause.setOnFinished(e -> ScreenNavigator.navigateToMainMenu());
                pause.play();
            }
        }
    }
}
