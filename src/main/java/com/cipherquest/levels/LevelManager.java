package com.cipherquest.levels;

import com.cipherquest.levels.caesar.CaesarLevel;
import com.cipherquest.levels.database.DatabaseLevel;
import com.cipherquest.levels.linux.LinuxLevel;

/**
 * [ENCAPSULATION] Factory สร้าง Level object ตามหมายเลข checkpoint
 *
 * หน้าที่เดียวของคลาสนี้คือ "รู้ว่าด่านไหนเป็น class อะไร"
 * และคืน Level interface กลับไป — ผู้เรียกไม่ต้องรู้ว่าได้ class อะไร
 *
 * ทำให้ GameEngine เรียกแค่ createLevel(id) แล้วเรียก level.checkAnswer()
 * โดยไม่ต้อง import CaesarLevel / LinuxLevel / DatabaseLevel เลย
 *
 * → โชว์: Encapsulation (ซ่อน mapping), Polymorphism (คืน Level interface)
 */
public class LevelManager {

    private static final int TOTAL_CHECKPOINTS = 7;

    /**
     * สร้างและคืน Level ที่ตรงกับหมายเลข checkpoint
     * subclass ที่ถูก return เป็น implementation detail — ซ่อนอยู่ใน method นี้
     *
     * @param checkpointId หมายเลขด่าน 1–7
     * @return Level object พร้อมใช้งาน (ยังไม่ได้ loadChallenge)
     * @throws IllegalArgumentException ถ้า checkpointId ไม่อยู่ในช่วง 1–7
     */
    public Level createLevel(int checkpointId) {
        return switch (checkpointId) {
            case 1 -> new CaesarLevel(checkpointId);
            case 2 -> new LinuxLevel(checkpointId);
            case 3, 4, 5, 6, 7 -> new DatabaseLevel(checkpointId);
            default -> throw new IllegalArgumentException(
                    "Checkpoint id ต้องอยู่ระหว่าง 1–" + TOTAL_CHECKPOINTS + " แต่ได้รับ: " + checkpointId
            );
        };
    }

    public int getTotalCheckpoints() {
        return TOTAL_CHECKPOINTS;
    }
}
