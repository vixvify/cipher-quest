package com.cipherquest.player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * [ENCAPSULATION - การห่อหุ้มข้อมูล]
 * คลาสเก็บข้อมูลและสถานะของผู้เล่น (Player State Entity)
 *
 * ฟิลด์ทุกตัวถูกตั้งเป็น private เพื่อป้องกันไม่ให้ภายนอกแก้ไขค่าโดยตรง
 * การเปลี่ยนแปลงคะแนน, ชีวิต, หรือการบันทึกด่านที่ผ่าน จะต้องกระทำผ่านเมธอดที่มีการตรวจสอบ (Business Invariants)
 */
public class Player {

    public static final int MAX_LIVES = 3;

    private final String username;
    private int lives;
    private int score;
    private int hintsUsed;
    private final Set<Integer> completedCheckpoints;

    public Player(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.username = "Agent";
        } else {
            this.username = username.trim();
        }
        this.completedCheckpoints = new HashSet<>();
        reset();
    }

    /**
     * คืนค่าสถานะเริ่มต้นสำหรับเริ่มเกมใหม่
     */
    public void reset() {
        this.lives = MAX_LIVES;
        this.score = 0;
        this.hintsUsed = 0;
        this.completedCheckpoints.clear();
    }

    /**
     * เพิ่มคะแนนให้ผู้เล่น (คะแนนต้องเป็นบวก)
     */
    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    /**
     * ลดพลังชีวิตของผู้เล่นเมื่อตอบผิด
     * ไม่ให้ชีวิตต่ำกว่า 0
     */
    public void deductLife() {
        if (this.lives > 0) {
            this.lives--;
        }
    }

    /**
     * บันทึกการขอคำใบ้ (Hint)
     */
    public void recordHintUsed() {
        this.hintsUsed++;
    }

    /**
     * บันทึกว่าผู้เล่นผ่าน checkpoint นี้แล้ว
     */
    public void markCheckpointCompleted(int checkpointId) {
        this.completedCheckpoints.add(checkpointId);
    }

    /**
     * ตรวจสอบว่าผู้เล่นยังเหลือชีวิตอยู่หรือไม่
     */
    public boolean isAlive() {
        return this.lives > 0;
    }

    /**
     * ตรวจสอบว่าผ่าน checkpoint ที่กำหนดไปแล้วหรือยัง
     */
    public boolean hasCompletedCheckpoint(int checkpointId) {
        return this.completedCheckpoints.contains(checkpointId);
    }

    // ─── Getters (Encapsulation: อ่านได้อย่างเดียว ป้องกันการถูกแก้ไข) ───

    public String getUsername() {
        return username;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    /**
     * ส่งคืน Set ของ checkpoint แบบ unmodifiable view ป้องกันภายนอกแก้ไขชุดข้อมูลโดยตรง
     */
    public Set<Integer> getCompletedCheckpoints() {
        return Collections.unmodifiableSet(completedCheckpoints);
    }

    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", lives=" + lives +
                ", score=" + score +
                ", hintsUsed=" + hintsUsed +
                ", completedCheckpoints=" + completedCheckpoints.size() +
                '}';
    }
}

