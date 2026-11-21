package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScreenFxmlLoadTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
        GameEngine.getInstance().startNewGame();
    }

    @Test
    void testLoadMainMenuFxml() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-menu.fxml"));
        Object root = loader.load();
        assertNotNull(root);
    }

    @Test
    void testLoadPasskeyScreenFxml() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/passkey-screen.fxml"));
        Object root = loader.load();
        assertNotNull(root);
    }

    @Test
    void testLoadTerminalScreenFxml() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/terminal-screen.fxml"));
        Object root = loader.load();
        assertNotNull(root);
    }

    @Test
    void testLoadSqlWorkbenchScreenFxml() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sql-workbench-screen.fxml"));
        Object root = loader.load();
        assertNotNull(root);
    }

    @Test
    void testLoadVictoryScreenFxml() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/victory-screen.fxml"));
        Object root = loader.load();
        assertNotNull(root);
    }

    @Test
    void sqlWorkbenchRendersTheActiveMissionSchemaAndQueryResult() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            GameEngine engine = GameEngine.getInstance();
            engine.startNewGame();
            engine.getPlayer().unlockCheckpoint(3);
            assertTrue(engine.selectCheckpoint(3));

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sql-workbench-screen.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 900, 600);
                for (String stylesheet : new String[]{"monitor-shared.css", "sql-workbench-screen.css"}) {
                    scene.getStylesheets().add(getClass().getResource("/css/" + stylesheet).toExternalForm());
                }
                root.applyCss();
                root.layout();

                Label checkpoint = (Label) root.lookup("#checkpointLabel");
                VBox tables = (VBox) root.lookup("#schemaTables");
                assertEquals("CHECKPOINT 3 / 7", checkpoint.getText());
                assertTrue(tables.getChildren().stream()
                        .anyMatch(node -> node instanceof Label label && label.getText().equals("explorers")));

                TextArea query = (TextArea) root.lookup("#queryArea");
                query.setText("SELECT 1 AS example");
                ((Button) root.lookup("#runButton")).fire();

                assertEquals("1 rows", ((Label) root.lookup("#rowCountLabel")).getText());
                assertTrue(((Label) root.lookup("#feedbackLabel")).getText().contains("ยังไม่ถูกต้อง"));
            } finally {
                engine.startNewGame();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }
}

