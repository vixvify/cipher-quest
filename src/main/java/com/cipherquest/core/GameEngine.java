package com.cipherquest.core;

import com.cipherquest.levels.Level;
import com.cipherquest.levels.LevelManager;
import com.cipherquest.player.Player;

import java.util.ArrayList;
import java.util.List;

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

    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

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

    public void startNewGame() {
        startNewGame("Agent");
    }

    public void startNewGame(String playerName) {
        this.player = new Player(playerName);
        this.currentCheckpoint = STARTING_CHECKPOINT;
        loadCheckpoint(this.currentCheckpoint);
    }

    private void loadCheckpoint(int checkpointId) {
        this.currentLevel = levelManager.createLevel(checkpointId);
        this.currentLevel.loadChallenge();
        changeGameState(GameState.PLAYING);

        for (GameEventListener listener : eventListeners) {
            listener.onLevelLoaded(this.currentLevel);
            listener.onScoreChanged(this.player.getScore(), 0);
        }
    }

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
                        currentLevel.getSuccessFeedback(),
                        earnedScore,
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                return result;
            }
        } else {
            AnswerResult result = AnswerResult.wrong(
                    currentLevel.getFailureFeedback(),
                    player.getScore()
            );
            notifyAnswerEvaluated(result);
            return result;
        }
    }

    private void notifyAnswerEvaluated(AnswerResult result) {
        for (GameEventListener listener : eventListeners) {
            listener.onAnswerEvaluated(result);
        }
    }

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

    public void retryCurrentCheckpoint() {
        if (player != null && currentCheckpoint >= 1) {
            loadCheckpoint(currentCheckpoint);
        }
    }

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
