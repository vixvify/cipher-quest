package com.cipherquest.levels;

/**
 * [ABSTRACTION + INHERITANCE] Abstract class แม่แบบสำหรับทุกด่าน
 *
 * ทำหน้าที่เก็บ state และ logic ที่ "เหมือนกันทุกด่าน" ไว้ที่เดียว
 * ด่านที่ extends จะได้ field และ method เหล่านี้ไปใช้ฟรี (Inheritance)
 * และต้อง implement เฉพาะส่วนที่ "ต่างกัน" ตาม @Override (Polymorphism)
 *
 * → โชว์: Abstraction, Inheritance, Encapsulation
 */
public abstract class BaseLevel implements Level {

    // [ENCAPSULATION] state ภายในถูกซ่อนด้วย private
    // ทุก subclass เข้าถึงผ่าน getter/setter เท่านั้น
    private final int checkpointId;
    private String title;
    private String description;
    private String hint;
    private boolean completed;
    private int attempts;
    private boolean hintUsed;

    protected BaseLevel(int checkpointId) {
        this.checkpointId = checkpointId;
        this.completed = false;
        this.attempts = 0;
        this.hintUsed = false;
    }

    // ─────────────────────────────────────────────
    // เมธอดจาก Level interface ที่ตอบได้จาก state
    // (ไม่ต้อง override ใน subclass)
    // ─────────────────────────────────────────────

    @Override
    public String getTitle() { return title; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getHint() { return hint; }

    @Override
    public int getCheckpointId() { return checkpointId; }

    // ─────────────────────────────────────────────
    // Setters สำหรับ subclass ใช้ตอน loadChallenge()
    // ─────────────────────────────────────────────

    protected void setTitle(String title) { this.title = title; }
    protected void setDescription(String description) { this.description = description; }
    protected void setHint(String hint) { this.hint = hint; }

    // ─────────────────────────────────────────────
    // Business logic ที่ใช้ร่วมกันทุกด่าน
    // ─────────────────────────────────────────────

    /**
     * เรียกก่อน checkAnswer() เพื่อนับจำนวนครั้งที่พยายาม
     * subclass เรียก super.recordAttempt() ใน checkAnswer() ได้
     */
    protected void recordAttempt() {
        attempts++;
    }

    /**
     * ทำเครื่องหมายว่าด่านนี้ผ่านแล้ว
     * GameEngine เรียกหลัง checkAnswer() คืน true
     */
    public void markCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() { return completed; }

    @Override
    public int getAttempts() { return attempts; }

    @Override
    public boolean useHint() {
        if (!this.hintUsed) {
            this.hintUsed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasUsedHint() {
        return hintUsed;
    }

    // ─────────────────────────────────────────────
    // เมธอด abstract ที่ subclass ต้อง implement เอง
    // เพราะแต่ละด่านทำ "ต่างกัน" (Polymorphism)
    // ─────────────────────────────────────────────

    /**
     * โหลดข้อมูลโจทย์ที่เฉพาะของด่านนั้น
     * เช่น CaesarLevel โหลด ciphertext+shift, LinuxLevel โหลด accepted_commands
     */
    @Override
    public abstract void loadChallenge();

    /**
     * ตรวจสอบคำตอบด้วยวิธีที่ต่างกันตามแต่ละด่าน
     */
    @Override
    public abstract boolean checkAnswer(String input);

    /**
     * คืนประเภทหน้าจอที่ด่านนี้ต้องการ
     */
    @Override
    public abstract ScreenType getScreenType();
}

