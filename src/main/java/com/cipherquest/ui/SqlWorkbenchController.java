package com.cipherquest.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้า SQL Workbench Screen (ด่าน 3–7 — SQL Query)
 * ทำหน้าที่รับ SQL Query จากผู้เล่น รันกับ SQLite จำลอง
 * แสดงผลในตาราง และตรวจสอบความถูกต้องกับ DatabaseLevel
 */
public class SqlWorkbenchController implements Initializable {

    @FXML private TextArea queryArea;
    @FXML private TableView<Object> resultTable;
    @FXML private Label feedbackLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label checkpointLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: โหลดโจทย์จาก challenge_0X_db.yaml ตามหมายเลขด่าน
        // TODO: สร้าง SQLite in-memory database จากข้อมูลโจทย์
    }

    @FXML
    private void onRunQuery() {
        // TODO: รัน SQL Query กับ SQLite และแสดงผลใน resultTable
    }

    @FXML
    private void onClearQuery() {
        queryArea.clear();
        feedbackLabel.setText("");
        rowCountLabel.setText("0 rows");
    }
}

