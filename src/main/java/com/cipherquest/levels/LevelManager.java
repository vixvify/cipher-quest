package com.cipherquest.levels;

/**
 * ตัวจัดการด่านทั้งหมด (Level Manager)
 * ไฟล์นี้ทำหน้าที่เป็นตัวกลางควบคุมการไหลของด่าน (Game Flow)
 *
 * หน้าที่:
 * - เก็บสถานะปัจจุบันของผู้เล่น เช่น ด่านที่กำลังเล่น คะแนน ความพยายาม
 * - สั่งโหลดด่านถัดไปเมื่อผู้เล่นผ่านด่านปัจจุบัน
 * - แจกแจงว่าด่านไหนใช้ UI แบบไหน (Passkey / Terminal / SQL Workbench)
 *
 * ลำดับด่าน:
 *   ด่าน 1  → CaesarLevel   → PasskeyScreen
 *   ด่าน 2  → LinuxLevel    → TerminalScreen
 *   ด่าน 3–7 → DatabaseLevel → SqlWorkbenchScreen
 */
public class LevelManager {
    // TODO: implement level management and game flow
}

