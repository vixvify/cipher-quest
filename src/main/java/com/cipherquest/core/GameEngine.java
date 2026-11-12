package com.cipherquest.core;

import com.cipherquest.levels.Level;
import com.cipherquest.levels.LevelManager;

/**
 * [ENCAPSULATION] แกนกลางควบคุมเกมทั้งหมด (Game Coordinator)
 *
 * GameEngine รู้จักเฉพาะ Level interface — ไม่รู้จัก CaesarLevel / LinuxLevel / DatabaseLevel
 * ทั้ง state ของเกม (score, checkpoint, lives) ถูกซ่อนภายใน private field
 * และเปิดให้เข้าถึงผ่าน method ที่ควบคุมได้เท่านั้น
 *
 * ─── แบ่งหน้าที่ให้ชัดเจน ───────────────────────────────────────────
 *  GameEngine   → "สมอง" ของเกม: state, score, flow, scene transition
 *  LevelManager → "โรงงาน": สร้าง Level object ตาม checkpoint id
 *  BaseLevel    → "แม่แบบ": state และ logic ที่ทุกด่านมีร่วมกัน
 *  CaesarLevel / LinuxLevel / DatabaseLevel → "ด่านจริง": logic เฉพาะของแต่ละด่าน
 * ─────────────────────────────────────────────────────────────────────
 *
 * → โชว์: Encapsulation (ซ่อน state), Polymorphism (เรียกผ่าน Level interface)
 */
public class GameEngine {

    // ─── State ของเกม (Encapsulation: ทั้งหมด private) ───

    /** หมายเลข checkpoint ปัจจุบัน (1–7) */
    private int currentCheckpoint;

    /** คะแนนสะสมของผู้เล่น */
    private int score;

    /** จำนวนชีวิตที่เหลือ */
    private int lives;

    /** ด่านที่กำลังเล่นอยู่ (เป็น Level interface — ไม่รู้ว่า class อะไร) */
    private Level currentLevel;

    /** LevelManager ไว้สร้าง Level ใหม่ */
    private final LevelManager levelManager;

    // ─── ค่า default ───

    private static final int STARTING_CHECKPOINT = 1;
    private static final int STARTING_LIVES = 3;
    private static final int SCORE_PER_CORRECT = 100;
    private static final int SCORE_BONUS_NO_HINT = 50;

    // ─── Singleton pattern (optional — เอาไว้ถ้าทีมอยากใช้) ───

    private static GameEngine instance;

    private GameEngine() {
        this.levelManager = new LevelManager();
        reset();
    }

    /** คืน instance เดียวของ GameEngine (Singleton) */
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────
    // Game Flow Methods — UI Controller เรียกใช้เมธอดเหล่านี้
    // ─────────────────────────────────────────────────────────

    /**
     * เริ่มเกมใหม่ตั้งแต่ต้น
     * เรียกจาก MainMenuController ตอนกด "START GAME"
     */
    public void startNewGame() {
        reset();
        loadCheckpoint(currentCheckpoint);
    }

    /**
     * รับคำตอบจากผู้เล่นและตรวจสอบ
     * UI Controller เรียกเมธอดนี้เมื่อผู้เล่นกด Submit/Execute/Run
     *
     * [POLYMORPHISM] เรียก currentLevel.checkAnswer() —
     * runtime จะเลือกเรียก CaesarLevel / LinuxLevel / DatabaseLevel
     * ให้โดยอัตโนมัติตาม object จริงที่อยู่ใน currentLevel
     *
     * @param input คำตอบของผู้เล่น
     * @return AnswerResult บอกว่าถูก/ผิด/จบเกม
     */
    public AnswerResult submitAnswer(String input) {
        if (currentLevel == null) return AnswerResult.ERROR;

        boolean correct = currentLevel.checkAnswer(input);

        if (correct) {
            score += SCORE_PER_CORRECT;
            currentLevel.markCompleted();

            if (currentCheckpoint >= levelManager.getTotalCheckpoints()) {
                return AnswerResult.GAME_CLEARED;
            }
            return AnswerResult.CORRECT;
        } else {
            lives--;
            if (lives <= 0) {
                return AnswerResult.GAME_OVER;
            }
            return AnswerResult.WRONG;
        }
    }

    /**
     * โหลดด่านถัดไปหลังจาก submitAnswer คืน CORRECT
     * เรียกจาก UI Controller หลังแสดง transition animation เสร็จ
     */
    public void advanceToNextCheckpoint() {
        if (currentCheckpoint < levelManager.getTotalCheckpoints()) {
            currentCheckpoint++;
            loadCheckpoint(currentCheckpoint);
        }
    }

    /**
     * โหลด checkpoint ตามหมายเลข
     * ใช้ LevelManager สร้าง Level แล้วเรียก loadChallenge()
     */
    private void loadCheckpoint(int checkpointId) {
        // LevelManager คืน Level interface — GameEngine ไม่รู้ว่าเป็น class อะไร
        currentLevel = levelManager.createLevel(checkpointId);
        currentLevel.loadChallenge();
    }

    /**
     * Reset state กลับไปค่าเริ่มต้น
     */
    private void reset() {
        currentCheckpoint = STARTING_CHECKPOINT;
        score = 0;
        lives = STARTING_LIVES;
        currentLevel = null;
    }

    // ─── Getters (Encapsulation: อ่านได้แต่แก้ไขตรงไม่ได้) ───

    public int getCurrentCheckpoint() { return currentCheckpoint; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public Level getCurrentLevel() { return currentLevel; }
    public int getTotalCheckpoints() { return levelManager.getTotalCheckpoints(); }

    // ─────────────────────────────────────────────────────────
    // Result enum — บอก UI ว่าต้องแสดงผลอะไรต่อ
    // ─────────────────────────────────────────────────────────

    /**
     * ผลลัพธ์จากการ submit คำตอบ
     * UI Controller รับค่านี้แล้วตัดสินใจเองว่าจะแสดงอะไร
     */
    public enum AnswerResult {
        /** คำตอบถูก — ให้แสดง success animation แล้วเรียก advanceToNextCheckpoint() */
        CORRECT,
        /** คำตอบผิด — ให้แสดง error message และ lives ที่เหลือ */
        WRONG,
        /** ผ่านทุกด่านครบแล้ว — ให้แสดงหน้า Victory */
        GAME_CLEARED,
        /** หมด lives — ให้แสดงหน้า Game Over */
        GAME_OVER,
        /** เรียก submitAnswer โดยที่ยังไม่ได้ startNewGame */
        ERROR
    }
}
