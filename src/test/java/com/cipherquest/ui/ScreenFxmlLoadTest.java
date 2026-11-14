package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
