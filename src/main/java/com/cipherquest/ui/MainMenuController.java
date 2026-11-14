package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Canvas matrixCanvas;

    private static final String GLYPHS = "010101017F4CAE298F#*+=-~/\\%@!&$?ABCDEF";
    private final Random random = new Random();
    private int[] dropPositions;
    private AnimationTimer matrixTimer;
    private long lastUpdate = 0;
    private static final int FONT_SIZE = 14;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMatrixCanvas();
    }

    private void setupMatrixCanvas() {
        if (matrixCanvas == null || rootPane == null) return;
        matrixCanvas.widthProperty().bind(rootPane.widthProperty());
        matrixCanvas.heightProperty().bind(rootPane.heightProperty());
        matrixCanvas.widthProperty().addListener((obs, o, n) -> initDrops());
        initDrops();
        startMatrixRain();
    }

    private void initDrops() {
        int columns = (int) Math.max(1, matrixCanvas.getWidth() / FONT_SIZE);
        dropPositions = new int[columns];
        for (int i = 0; i < columns; i++) {
            dropPositions[i] = random.nextInt(80);
        }
    }

    private void startMatrixRain() {
        matrixTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate < 50_000_000L) return;
                lastUpdate = now;
                renderMatrix();
            }
        };
        matrixTimer.start();
    }

    private void renderMatrix() {
        double w = matrixCanvas.getWidth();
        double h = matrixCanvas.getHeight();
        if (w <= 0 || h <= 0 || dropPositions == null) return;

        GraphicsContext gc = matrixCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(6, 11, 19, 0.10));
        gc.fillRect(0, 0, w, h);
        gc.setFont(Font.font("Consolas", FONT_SIZE));

        int columns = Math.min(dropPositions.length, (int) (w / FONT_SIZE));
        for (int i = 0; i < columns; i++) {
            char ch = GLYPHS.charAt(random.nextInt(GLYPHS.length()));
            double x = i * FONT_SIZE;
            double y = dropPositions[i] * FONT_SIZE;

            if (random.nextInt(12) == 0) {
                gc.setFill(Color.rgb(200, 240, 230, 0.6));
            } else {
                gc.setFill(Color.rgb(0, 180, 130, 0.25));
            }
            gc.fillText(String.valueOf(ch), x, y);

            if (y > h && random.nextDouble() > 0.975) {
                dropPositions[i] = 0;
            } else {
                dropPositions[i]++;
            }
        }
    }

    @FXML
    private void onStartGame() {
        GameEngine.getInstance().startNewGame();
        ScreenNavigator.navigateToCurrentLevel();
    }
}
