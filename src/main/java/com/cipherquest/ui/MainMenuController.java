package com.cipherquest.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Canvas matrixCanvas;
    @FXML private Label subtitleLabel;
    @FXML private Label tickerLabel;
    @FXML private Label cursorLabel;
    @FXML private Button soundButton;
    @FXML private Button fullscreenButton;

    // Modals
    @FXML private StackPane modalOverlay;
    @FXML private VBox cipherLabModal;
    @FXML private VBox levelSelectModal;
    @FXML private VBox settingsModal;

    // Cipher Lab Controls
    @FXML private TextField labInputField;
    @FXML private Slider labShiftSlider;
    @FXML private Label labShiftValue;
    @FXML private TextField labOutputField;

    // Settings Controls
    @FXML private Slider masterVolumeSlider;
    @FXML private Label masterVolumeLabel;
    @FXML private Slider musicVolumeSlider;
    @FXML private Label musicVolumeLabel;
    @FXML private Slider sfxVolumeSlider;
    @FXML private Label sfxVolumeLabel;
    @FXML private CheckBox matrixRainToggle;

    private boolean isMuted = false;
    private boolean isMatrixRainEnabled = true;
    private MatrixRainEffect matrixRainEffect;

    private static final String TARGET_SUBTITLE = "[ DECRYPT THE PAST • ENCODE THE FUTURE ]";
    private static final String CIPHER_CHAR_POOL = "01010101#@$%&*!?ABCDEF0123456789§ΨΩλ∆∑";
    private final Random random = new Random();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMatrixCanvas();
        setupDecryptAnimation();
        setupBlinkingCursor();
        setupCipherLab();
        setupSettingsSliders();
        setupKeyboardShortcuts();
    }

    // ==========================================
    // MATRIX RAIN CANVAS EFFECT
    // ==========================================
    private void setupMatrixCanvas() {
        if (matrixCanvas != null && rootPane != null) {
            matrixCanvas.widthProperty().bind(rootPane.widthProperty());
            matrixCanvas.heightProperty().bind(rootPane.heightProperty());

            matrixRainEffect = new MatrixRainEffect(matrixCanvas);
            matrixRainEffect.start();
        }
    }

    // ==========================================
    // DECRYPTION TEXT EFFECT
    // ==========================================
    private void setupDecryptAnimation() {
        if (subtitleLabel == null) return;

        final int totalLength = TARGET_SUBTITLE.length();
        final char[] currentChars = new char[totalLength];
        for (int i = 0; i < totalLength; i++) {
            currentChars[i] = CIPHER_CHAR_POOL.charAt(random.nextInt(CIPHER_CHAR_POOL.length()));
        }

        final int[] revealedCount = {0};
        Timeline timeline = new Timeline();
        KeyFrame kf = new KeyFrame(Duration.millis(45), event -> {
            for (int i = revealedCount[0]; i < totalLength; i++) {
                if (TARGET_SUBTITLE.charAt(i) == ' ' || TARGET_SUBTITLE.charAt(i) == '•') {
                    currentChars[i] = TARGET_SUBTITLE.charAt(i);
                } else {
                    currentChars[i] = CIPHER_CHAR_POOL.charAt(random.nextInt(CIPHER_CHAR_POOL.length()));
                }
            }

            if (revealedCount[0] < totalLength) {
                currentChars[revealedCount[0]] = TARGET_SUBTITLE.charAt(revealedCount[0]);
                revealedCount[0]++;
            }

            subtitleLabel.setText(new String(currentChars));
        });

        timeline.getKeyFrames().add(kf);
        timeline.setCycleCount(totalLength + 10);
        timeline.play();
    }

    // ==========================================
    // BLINKING TERMINAL CURSOR
    // ==========================================
    private void setupBlinkingCursor() {
        if (cursorLabel == null) return;
        Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    String current = cursorLabel.getText();
                    if (current.endsWith("_")) {
                        cursorLabel.setText(current.substring(0, current.length() - 1) + " ");
                    } else {
                        cursorLabel.setText(current.substring(0, current.length() - 1) + "_");
                    }
                })
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.play();
    }

    // ==========================================
    // LIVE CIPHER LAB (Interactive Gimmick)
    // ==========================================
    private void setupCipherLab() {
        if (labShiftSlider != null && labShiftValue != null) {
            labShiftSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int shift = newVal.intValue();
                labShiftValue.setText("Shift: " + shift);
                updateCipherLabResult();
            });
        }
        updateCipherLabResult();
    }

    @FXML
    private void onCipherTestInput() {
        updateCipherLabResult();
    }

    private void updateCipherLabResult() {
        if (labInputField == null || labOutputField == null || labShiftSlider == null) return;
        String text = labInputField.getText();
        if (text == null) text = "";

        int shift = (int) labShiftSlider.getValue();
        StringBuilder cipherResult = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                char shifted = (char) ('A' + (c - 'A' + shift) % 26);
                cipherResult.append(shifted);
            } else if (Character.isLowerCase(c)) {
                char shifted = (char) ('a' + (c - 'a' + shift) % 26);
                cipherResult.append(shifted);
            } else {
                cipherResult.append(c);
            }
        }
        labOutputField.setText(cipherResult.toString());
    }

    // ==========================================
    // SETTINGS SLIDERS
    // ==========================================
    private void setupSettingsSliders() {
        if (masterVolumeSlider != null && masterVolumeLabel != null) {
            masterVolumeSlider.valueProperty().addListener((obs, oldV, newV) ->
                    masterVolumeLabel.setText(newV.intValue() + "%"));
        }
        if (musicVolumeSlider != null && musicVolumeLabel != null) {
            musicVolumeSlider.valueProperty().addListener((obs, oldV, newV) ->
                    musicVolumeLabel.setText(newV.intValue() + "%"));
        }
        if (sfxVolumeSlider != null && sfxVolumeLabel != null) {
            sfxVolumeSlider.valueProperty().addListener((obs, oldV, newV) ->
                    sfxVolumeLabel.setText(newV.intValue() + "%"));
        }
    }

    private void setupKeyboardShortcuts() {
        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            onCloseOverlay();
                        }
                    });
                }
            });
        }
    }

    // ==========================================
    // MENU ACTIONS
    // ==========================================
    @FXML
    private void onStartGame() {
        System.out.println("[CipherQuest] Initiating Mission 1: Caesar's Secret...");
        if (cursorLabel != null) {
            cursorLabel.setText("launching_mission_1...");
        }
        // Future scene transition can be hooked here seamlessly
    }

    @FXML
    private void onLevelSelect() {
        showModal(levelSelectModal);
    }

    @FXML
    private void onSelectLevel1() {
        System.out.println("[CipherQuest] Selected Level 1 from Level Select menu");
        onCloseOverlay();
        onStartGame();
    }

    @FXML
    private void onHowToPlay() {
        showModal(cipherLabModal);
    }

    @FXML
    private void onSettings() {
        showModal(settingsModal);
    }

    @FXML
    private void onCloseOverlay() {
        if (modalOverlay == null || !modalOverlay.isVisible()) return;

        FadeTransition fade = new FadeTransition(Duration.millis(180), modalOverlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            modalOverlay.setVisible(false);
            if (cipherLabModal != null) cipherLabModal.setVisible(false);
            if (levelSelectModal != null) levelSelectModal.setVisible(false);
            if (settingsModal != null) settingsModal.setVisible(false);
        });
        fade.play();
    }

    private void showModal(Pane modalContent) {
        if (modalOverlay == null || modalContent == null) return;

        if (cipherLabModal != null) cipherLabModal.setVisible(false);
        if (levelSelectModal != null) levelSelectModal.setVisible(false);
        if (settingsModal != null) settingsModal.setVisible(false);

        modalContent.setVisible(true);
        modalOverlay.setOpacity(0.0);
        modalOverlay.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(220), modalOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    @FXML
    private void onToggleSound() {
        isMuted = !isMuted;
        if (soundButton != null) {
            if (isMuted) {
                soundButton.setText("🔇 AUDIO: OFF");
                soundButton.getStyleClass().add("button-muted");
            } else {
                soundButton.setText("🔊 AUDIO: ON");
                soundButton.getStyleClass().remove("button-muted");
            }
        }
    }

    @FXML
    private void onToggleFullscreen() {
        if (rootPane != null && rootPane.getScene() != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (stage != null) {
                stage.setFullScreen(!stage.isFullScreen());
                if (fullscreenButton != null) {
                    fullscreenButton.setText(stage.isFullScreen() ? "⛶ WINDOWED" : "⛶ FULLSCREEN");
                }
            }
        }
    }

    @FXML
    private void onToggleMatrixRain() {
        if (matrixRainToggle != null) {
            isMatrixRainEnabled = matrixRainToggle.isSelected();
            if (matrixRainEffect != null) {
                if (isMatrixRainEnabled) {
                    matrixRainEffect.start();
                } else {
                    matrixRainEffect.stop();
                    GraphicsContext gc = matrixCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, matrixCanvas.getWidth(), matrixCanvas.getHeight());
                }
            }
        }
    }

    @FXML
    private void onExit() {
        System.out.println("[CipherQuest] Exiting application...");
        Platform.exit();
        System.exit(0);
    }

    // ==========================================
    // MATRIX RAIN BACKGROUND ANIMATION ENGINE
    // ==========================================
    private static class MatrixRainEffect {
        private final Canvas canvas;
        private final Random rand = new Random();
        private AnimationTimer timer;
        private int[] dropPositions;
        private long lastUpdate = 0;

        private static final int FONT_SIZE = 14;
        private static final String GLYPHS = "010101017F4CAE298F#*+=-~/\\%@!&$?ABCDEF";

        public MatrixRainEffect(Canvas canvas) {
            this.canvas = canvas;
            initDrops();
            this.canvas.widthProperty().addListener((obs, o, n) -> initDrops());
        }

        private void initDrops() {
            int columns = (int) Math.max(1, canvas.getWidth() / FONT_SIZE);
            dropPositions = new int[columns];
            for (int i = 0; i < columns; i++) {
                dropPositions[i] = rand.nextInt(100);
            }
        }

        public void start() {
            if (timer != null) timer.stop();

            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (now - lastUpdate < 45_000_000L) { // ~22 FPS for smooth, retro terminal feel
                        return;
                    }
                    lastUpdate = now;
                    render();
                }
            };
            timer.start();
        }

        public void stop() {
            if (timer != null) {
                timer.stop();
            }
        }

        private void render() {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            if (width <= 0 || height <= 0 || dropPositions == null) return;

            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Semi-transparent dark wash to create fading trail effect
            gc.setFill(Color.rgb(5, 10, 18, 0.12));
            gc.fillRect(0, 0, width, height);

            gc.setFont(Font.font("Consolas", FONT_SIZE));

            int columns = Math.min(dropPositions.length, (int) (width / FONT_SIZE));
            for (int i = 0; i < columns; i++) {
                char ch = GLYPHS.charAt(rand.nextInt(GLYPHS.length()));
                double x = i * FONT_SIZE;
                double y = dropPositions[i] * FONT_SIZE;

                // Glowing head of the stream (bright cyan/green)
                if (rand.nextInt(10) == 0) {
                    gc.setFill(Color.rgb(180, 255, 235, 0.85));
                } else {
                    gc.setFill(Color.rgb(0, 220, 160, 0.45));
                }

                gc.fillText(String.valueOf(ch), x, y);

                if (y > height && rand.nextDouble() > 0.975) {
                    dropPositions[i] = 0;
                } else {
                    dropPositions[i]++;
                }
            }
        }
    }
}
