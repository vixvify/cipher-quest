package com.cipherquest.core;

import com.cipherquest.levels.Level;
import com.cipherquest.levels.LevelManager;
import com.cipherquest.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * [CORE GAME ENGINE - แกนกลางควบคุมระบบเกม]
 *
 * แสดงการประยุกต์ใช้ 4 เสาหลักของ OOP:
 *
 * 1. ENCAPSULATION (การห่อหุ้มข้อมูล):
 *    - ฟิลด์ทั้งหมดเป็น private (player, gameState, currentLevel, listeners)
 *    - ควบคุมการเปลี่ยนสถานะของเกมผ่าน State Machine อย่างรัดกุม ป้องกันสถานะที่ไม่ถูกต้อง
 *
 * 2. ABSTRACTION (การซ่อนรายละเอียดและการแยกส่วน):
 *    - GameEngine ติดต่อกับด่านผ่านอินเตอร์เฟส Level เท่านั้น
 *    - ติดต่อกับ UI ผ่าน Observer Pattern (GameEventListener) โดยไม่มีการ import JavaFX แม้แต่คลาสเดียว
 *    - ทำให้ Core Logic ทำงานเป็น Pure Java ที่สามารถทดสอบด้วย Unit Test ได้ 100%
 *
 * 3. INHERITANCE (การสืบทอดคุณสมบัติ):
 *    - ด่านต่างๆ สืบทอดความสามารถและสถานะพื้นฐานมาจาก BaseLevel
 *
 * 4. POLYMORPHISM (การมีหลายรูปแบบ):
 *    - การเรียก currentLevel.checkAnswer(input) จะถูกส่งต่อไปทำงานที่คลาสเฉพาะของด่านนั้นๆ แบบไดนามิก
 *    - การคำนวณคะแนนผ่านอินเตอร์เฟส ScoreCalculator (Strategy Pattern)
 */
public class GameEngine {

    private static GameEngine instance;

    private Player player;
    private GameState gameState;
    private int currentCheckpoint;
    private Level currentLevel;

    private final LevelManager levelManager;
    private final ScoreCalculator scoreCalculator;
    private final List<GameEventListener> eventListeners;

    private static final int STARTING_CHECKPOINT = 1;

    private GameEngine() {
        this.levelManager = new LevelManager();
        this.scoreCalculator = new DefaultScoreCalculator();
        this.eventListeners = new ArrayList<>();
        this.gameState = GameState.NOT_STARTED;
    }

    /**
     * Singleton Instance สำหรับเข้าถึง GameEngine จากทุกที่ในโปรเจกต์
     */
    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────
    // Observer Pattern (การจัดการ Listener)
    // ─────────────────────────────────────────────────────────

    public void addListener(GameEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public void removeListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }

    private void changeGameState(GameState newState) {
        if (this.gameState != newState) {
            GameState oldState = this.gameState;
            this.gameState = newState;
            for (GameEventListener listener : eventListeners) {
                listener.onGameStateChanged(oldState, newState);
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Game Flow Lifecycle Methods
    // ─────────────────────────────────────────────────────────

    /**
     * เริ่มต้นเกมใหม่
     */
    public void startNewGame() {
        startNewGame("Agent");
    }

    /**
     * เริ่มต้นเกมใหม่โดยระบุชื่อผู้เล่น
     */
    public void startNewGame(String playerName) {
        this.player = new Player(playerName);
        this.currentCheckpoint = STARTING_CHECKPOINT;
        loadCheckpoint(this.currentCheckpoint);
    }

    /**
     * โหลดข้อมูลด่านตามหมายเลข checkpoint
     */
    private void loadCheckpoint(int checkpointId) {
        this.currentLevel = levelManager.createLevel(checkpointId);
        this.currentLevel.loadChallenge();
        changeGameState(GameState.PLAYING);

        for (GameEventListener listener : eventListeners) {
            listener.onLevelLoaded(this.currentLevel);
            listener.onLivesChanged(this.player.getLives());
            listener.onScoreChanged(this.player.getScore(), 0);
        }
    }

    /**
     * รับและประเมินคำตอบของผู้เล่น
     *
     * [POLYMORPHISM] เรียก currentLevel.checkAnswer(input) ซึ่งจะเรียกใช้
     * ตรรกะของ CaesarLevel, LinuxLevel หรือ DatabaseLevel ตามอ็อบเจกต์จริงขณะรันไทม์
     */
    public AnswerResult submitAnswer(String input) {
        if (gameState != GameState.PLAYING || currentLevel == null) {
            return AnswerResult.error("เกมไม่ได้อยู่ในสถานะที่สามารถส่งคำตอบได้ในขณะนี้");
        }

        boolean isCorrect = currentLevel.checkAnswer(input);

        if (isCorrect) {
            currentLevel.markCompleted();
            player.markCheckpointCompleted(currentCheckpoint);

            int earnedScore = scoreCalculator.calculateScore(
                    currentCheckpoint,
                    currentLevel.getAttempts(),
                    currentLevel.hasUsedHint()
            );
            player.addScore(earnedScore);

            for (GameEventListener listener : eventListeners) {
                listener.onScoreChanged(player.getScore(), earnedScore);
            }

            if (currentCheckpoint >= levelManager.getTotalCheckpoints()) {
                changeGameState(GameState.VICTORY);
                AnswerResult result = AnswerResult.gameCleared(
                        "ยินดีด้วย! คุณเคลียร์ภารกิจครบทั้ง 7 Checkpoint และเจาะระบบสำเร็จ!",
                        earnedScore,
                        player.getLives(),
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                for (GameEventListener listener : eventListeners) {
                    listener.onGameCompleted(player);
                }
                return result;
            } else {
                changeGameState(GameState.CHECKPOINT_CLEARED);
                AnswerResult result = AnswerResult.correct(
                        "คำตอบถูกต้อง! ปลดล็อก Checkpoint " + currentCheckpoint + " สำเร็จ",
                        earnedScore,
                        player.getLives(),
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                return result;
            }
        } else {
            player.deductLife();

            for (GameEventListener listener : eventListeners) {
                listener.onLivesChanged(player.getLives());
            }

            if (!player.isAlive()) {
                changeGameState(GameState.GAME_OVER);
                AnswerResult result = AnswerResult.gameOver(
                        "การเข้าถึงถูกปฏิเสธ! พลังชีวิตหมดลงแล้ว (GAME OVER)",
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                for (GameEventListener listener : eventListeners) {
                    listener.onGameOver(player);
                }
                return result;
            } else {
                AnswerResult result = AnswerResult.wrong(
                        "รหัสผ่านหรือคำสั่งไม่ถูกต้อง! เหลือชีวิตอีก " + player.getLives() + " ครั้ง",
                        player.getLives(),
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                return result;
            }
        }
    }

    private void notifyAnswerEvaluated(AnswerResult result) {
        for (GameEventListener listener : eventListeners) {
            listener.onAnswerEvaluated(result);
        }
    }

    /**
     * ก้าวสู่ด่านถัดไป (เรียกเมื่อผ่านด่านปัจจุบันแล้ว)
     */
    public boolean advanceToNextCheckpoint() {
        if (gameState != GameState.CHECKPOINT_CLEARED) {
            return false;
        }

        if (currentCheckpoint < levelManager.getTotalCheckpoints()) {
            currentCheckpoint++;
            loadCheckpoint(currentCheckpoint);
            return true;
        }
        return false;
    }

    /**
     * ขอรับคำใบ้สำหรับด่านปัจจุบัน
     */
    public String requestHint() {
        if (currentLevel == null) {
            return "ไม่มีคำใบ้ในขณะนี้";
        }

        boolean firstTime = currentLevel.useHint();
        if (firstTime && player != null) {
            player.recordHintUsed();
        }

        String hint = currentLevel.getHint();
        for (GameEventListener listener : eventListeners) {
            listener.onHintUsed(hint);
        }
        return hint;
    }

    /**
     * รีสตาร์ทด่านปัจจุบันใหม่กรณีต้องการเริ่มแก้โจทย์ใหม่
     */
    public void retryCurrentCheckpoint() {
        if (player != null && player.isAlive() && currentCheckpoint >= 1) {
            loadCheckpoint(currentCheckpoint);
        }
    }

    // ─── Getters (Encapsulation: ป้องกันภายนอกเปลี่ยนค่าตามอำเภอใจ) ───

    public Player getPlayer() {
        return player;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getTotalCheckpoints() {
        return levelManager.getTotalCheckpoints();
    }
}
