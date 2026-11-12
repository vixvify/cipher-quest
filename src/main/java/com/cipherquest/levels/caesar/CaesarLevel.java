package com.cipherquest.levels.caesar;

import com.cipherquest.levels.BaseLevel;

/**
 * [INHERITANCE] ด่านที่ 1: Caesar Cipher
 *
 * extends BaseLevel → ได้ title, description, hint, attempts ฟรี
 * override checkAnswer() → ตรวจสอบด้วยวิธีเฉพาะของ Caesar (Polymorphism)
 *
 * UI ที่ใช้: PasskeyScreen
 */
public class CaesarLevel extends BaseLevel {

    // [ENCAPSULATION] ซ่อน ciphertext และ shift ไว้ภายใน
    private String ciphertext;
    private String expectedPlaintext;
    private int shift;

    public CaesarLevel(int checkpointId) {
        super(checkpointId);
    }

    /**
     * [POLYMORPHISM] โหลดโจทย์เฉพาะของ Caesar Cipher
     * TODO: อ่านจาก challenge_01_caesar.yaml
     */
    @Override
    public void loadChallenge() {
        // TODO: โหลดจาก YAML แล้ว set ค่าด้านล่าง
        setTitle("Operation Caesar");
        setDescription("ถอดรหัสข้อความที่ถูก encrypt ด้วย Caesar Cipher เพื่อค้นหา Passkey");
        setHint("ตัวอักษรแต่ละตัวถูกเลื่อนไปข้างหน้า 3 ตำแหน่ง ลองเลื่อนกลับ");
        this.ciphertext = "FLSKHUTZXHVW";
        this.expectedPlaintext = "CIPHERQUEST";
        this.shift = 3;
    }

    /**
     * [POLYMORPHISM] ตรวจสอบแบบ Caesar: เปรียบเทียบ plaintext ที่ถอดรหัสได้
     */
    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        return input != null && input.trim().equalsIgnoreCase(expectedPlaintext);
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.PASSKEY;
    }

    // Getter สำหรับ PasskeyScreenController
    public String getCiphertext() { return ciphertext; }
    public int getShift() { return shift; }
}
