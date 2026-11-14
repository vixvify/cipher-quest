package com.cipherquest.levels;

/**
 * [ABSTRACTION] Interface หลักของทุกด่านในเกม
 *
 * กำหนด "สัญญา" ที่ทุกด่านต้องทำได้ โดยไม่สนว่าข้างในจะทำงานอย่างไร
 * ผู้เรียกใช้ (GameEngine, LevelManager) รู้จักแค่ interface นี้
 * ไม่จำเป็นต้องรู้ว่ากำลังคุยกับ CaesarLevel, LinuxLevel หรือ DatabaseLevel
 *
 * → โชว์: Abstraction + Polymorphism
 */
public interface Level {

    /**
     * โหลดโจทย์ของด่านนี้จากไฟล์ challenge (data/challenges/)
     * เรียกครั้งเดียวก่อนเริ่มด่าน
     */
    void loadChallenge();

    /**
     * รับ input จากผู้เล่นแล้วตรวจสอบว่าถูกต้องหรือไม่
     * แต่ละด่านตรวจสอบ "แบบของตัวเอง" (Polymorphism)
     * - CaesarLevel: เปรียบเทียบ plaintext ที่ถอดรหัสได้
     * - LinuxLevel:  เปรียบเทียบคำสั่งกับ accepted_commands
     * - DatabaseLevel: รัน query แล้วเปรียบเทียบผลลัพธ์
     *
     * @param input คำตอบที่ผู้เล่นกรอก
     * @return true ถ้าถูกต้อง, false ถ้าผิด
     */
    boolean checkAnswer(String input);

    /**
     * คืนค่าคำใบ้ (hint) ของด่านนี้
     */
    String getHint();

    /**
     * ร้องขอและเปิดใช้งานคำใบ้สำหรับด่านนี้ (บันทึกว่าด่านนี้ใช้คำใบ้ไปแล้ว)
     * @return true หากเป็นการเปิดใช้ครั้งแรก, false หากเคยเปิดใช้ไปแล้ว
     */
    boolean useHint();

    /**
     * ตรวจสอบว่าด่านนี้ถูกใช้คำใบ้ไปแล้วหรือไม่
     */
    boolean hasUsedHint();

    /**
     * คืนจำนวนครั้งที่ผู้เล่นพยายามตอบในด่านนี้
     */
    int getAttempts();

    /**
     * คืนคำอธิบายโจทย์ที่แสดงให้ผู้เล่นเห็น
     */
    String getDescription();

    /**
     * คืนหัวเรื่องของด่าน เช่น "Operation Caesar", "Terminal Access"
     */
    String getTitle();

    /**
     * คืนหมายเลข checkpoint ของด่านนี้ (1–7)
     */
    int getCheckpointId();

    /**
     * ทำเครื่องหมายว่าด่านนี้ผ่านแล้ว
     * GameEngine เรียกหลัง checkAnswer() คืน true
     */
    void markCompleted();

    /**
     * คืนสถานะว่าด่านนี้ผ่านแล้วหรือยัง
     */
    boolean isCompleted();

    /**
     * คืนประเภทหน้าจอ UI ที่ด่านนี้ต้องใช้
     * GameEngine จะนำค่านี้ไปสั่ง scene transition
     */
    ScreenType getScreenType();

    /**
     * ประเภทหน้าจอที่แต่ละด่านต้องการ
     * GameEngine ใช้ค่านี้เพื่อสลับ scene ให้ถูกต้อง
     */
    enum ScreenType {
        /** หน้าจอกรอก Passkey (ด่าน 1 — Caesar) */
        PASSKEY,
        /** หน้าจอ Terminal / CMD (ด่าน 2 — Linux) */
        TERMINAL,
        /** หน้าจอ SQL Workbench (ด่าน 3–7 — Database) */
        SQL_WORKBENCH
    }
}
