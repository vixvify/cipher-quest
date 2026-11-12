package com.cipherquest.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้า Terminal Screen (ด่าน 2 — Linux Command Line)
 * ทำหน้าที่รับคำสั่ง Linux จากผู้เล่น จำลองผลลัพธ์ และส่งไปตรวจสอบกับ LinuxLevel
 */
public class TerminalScreenController implements Initializable {

    @FXML private TextField commandField;
    @FXML private VBox outputBox;
    @FXML private ScrollPane outputScrollPane;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: โหลดโจทย์จาก challenge_02_linux.yaml
    }

    @FXML
    private void onExecuteCommand() {
        // TODO: จำลองการรันคำสั่ง Linux และตรวจสอบความถูกต้อง
    }
}

