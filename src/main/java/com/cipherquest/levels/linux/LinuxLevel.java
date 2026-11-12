package com.cipherquest.levels.linux;

import com.cipherquest.levels.BaseLevel;

import java.util.List;

/**
 * [INHERITANCE] ด่านที่ 2: Linux Command Line
 *
 * extends BaseLevel → ได้ title, description, hint, attempts ฟรี
 * override checkAnswer() → ตรวจสอบด้วยการเปรียบเทียบคำสั่ง Linux (Polymorphism)
 *
 * UI ที่ใช้: TerminalScreen
 */
public class LinuxLevel extends BaseLevel {

    // [ENCAPSULATION] ซ่อน logic การตรวจสอบคำสั่งไว้ภายใน
    private List<String> acceptedCommands;
    private String simulatedOutput;
    private String task;

    public LinuxLevel(int checkpointId) {
        super(checkpointId);
    }

    /**
     * [POLYMORPHISM] โหลดโจทย์เฉพาะของ Linux Terminal
     * TODO: อ่านจาก challenge_02_linux.yaml
     */
    @Override
    public void loadChallenge() {
        // TODO: โหลดจาก YAML แล้ว set ค่าด้านล่าง
        setTitle("Terminal Access");
        setDescription("ค้นหาและแสดงเนื้อหาของไฟล์ secret.txt ที่ซ่อนอยู่ใน /home/agent");
        setHint("ใช้คำสั่ง cat เพื่ออ่านเนื้อหาไฟล์");
        this.task = "แสดงเนื้อหาของไฟล์ secret.txt ที่อยู่ใน /home/agent";
        this.acceptedCommands = List.of("cat /home/agent/secret.txt", "cat ~/secret.txt");
        this.simulatedOutput = "ACCESS_CODE: TOOR_7749";
    }

    /**
     * [POLYMORPHISM] ตรวจสอบแบบ Linux: เปรียบเทียบกับ accepted_commands
     * รองรับหลายคำสั่งที่ให้ผลลัพธ์เดียวกัน
     */
    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        if (input == null) return false;
        String trimmed = input.trim();
        return acceptedCommands.stream()
                .anyMatch(cmd -> cmd.equalsIgnoreCase(trimmed));
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.TERMINAL;
    }

    // Getter สำหรับ TerminalScreenController
    public String getTask() { return task; }
    public String getSimulatedOutput() { return simulatedOutput; }
}
