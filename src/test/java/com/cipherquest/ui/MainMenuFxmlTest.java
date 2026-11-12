package com.cipherquest.ui;

import org.junit.jupiter.api.Test;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MainMenuFxmlTest {

    @Test
    void testResourcesExist() {
        InputStream fxmlStream = getClass().getResourceAsStream("/fxml/main-menu.fxml");
        assertNotNull(fxmlStream, "main-menu.fxml should exist in classpath");

        InputStream cssStream = getClass().getResourceAsStream("/css/main-menu.css");
        assertNotNull(cssStream, "main-menu.css should exist in classpath");

        InputStream bgStream = getClass().getResourceAsStream("/images/background.png");
        assertNotNull(bgStream, "background.png should exist in classpath");
    }
}

