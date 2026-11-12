package com.cipherquest.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้า Passkey Screen (ด่าน 1 — Caesar Cipher)
 * ทำหน้าที่รับการกรอก Passkey จากผู้เล่น และส่งไปตรวจสอบกับ CaesarLevel
 */
public class PasskeyScreenController implements Initializable {

    @FXML private PasswordField passkeyField;
    @FXML private Label feedbackLabel;
    @FXML private Label hintLabel;
    @FXML private Label ciphertextLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: โหลดโจทย์จาก challenge_01_caesar.yaml
    }

    @FXML
    private void onSubmitPasskey() {
        // TODO: ตรวจสอบ passkey กับคำตอบของด่าน
    }
}

