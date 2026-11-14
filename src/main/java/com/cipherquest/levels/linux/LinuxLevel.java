package com.cipherquest.levels.linux;

import com.cipherquest.levels.BaseLevel;

import java.util.List;

public class LinuxLevel extends BaseLevel {

    private List<String> acceptedCommands;
    private String simulatedOutput;
    private String task;

    public LinuxLevel(int checkpointId) {
        super(checkpointId);
    }

    @Override
    public void loadChallenge() {
        setTitle("Terminal Access");
        setDescription("ค้นหาและแสดงเนื้อหาของไฟล์ secret.txt ที่ซ่อนอยู่ใน /home/agent");
        setHint("ใช้คำสั่ง cat เพื่ออ่านเนื้อหาไฟล์");
        this.task = "แสดงเนื้อหาของไฟล์ secret.txt ที่อยู่ใน /home/agent";
        this.acceptedCommands = List.of("cat /home/agent/secret.txt", "cat ~/secret.txt");
        this.simulatedOutput = "ACCESS_CODE: TOOR_7749";
    }

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

    public String getTask() {
        return task;
    }

    public String getSimulatedOutput() {
        return simulatedOutput;
    }
}
