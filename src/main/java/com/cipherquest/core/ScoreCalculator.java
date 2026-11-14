package com.cipherquest.core;

/**
 * [ABSTRACTION & POLYMORPHISM - กลยุทธ์การคำนวณคะแนน]
 * อินเตอร์เฟสสำหรับคำนวณคะแนนในแต่ละด่าน (Strategy Pattern)
 */
public interface ScoreCalculator {

    /**
     * คำนวณคะแนนที่ผู้เล่นได้รับจากการเคลียร์ด่าน
     *
     * @param checkpointId หมายเลขด่าน 1–7
     * @param attempts จำนวนครั้งที่ผู้เล่นพยายามตอบ
     * @param hintUsed ผู้เล่นเปิดใช้คำใบ้หรือไม่
     * @return คะแนนสุทธิที่ได้รับสำหรับด่านนี้
     */
    int calculateScore(int checkpointId, int attempts, boolean hintUsed);
}

