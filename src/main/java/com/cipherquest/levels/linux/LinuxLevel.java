package com.cipherquest.levels.linux;

import com.cipherquest.levels.BaseLevel;

public class LinuxLevel extends BaseLevel {

    private final VirtualTerminal terminal;

    public LinuxLevel(int checkpointId) {
        super(checkpointId);
        this.terminal = new VirtualTerminal();
    }

    @Override
    public void loadChallenge() {
        setTitle("Terminal Access");
        setDescription("ค้นหาโปรแกรมที่ซ่อนอยู่ในระบบแล้วรันขึ้นมาเพื่อเจาะระบบ");
        setHint("ลองสำรวจโฟลเดอร์ /opt หรือใช้คำสั่ง find / -name '*.bin' เพื่อค้นหาโปรแกรมแล้วรันด้วย ./");
        terminal.reset();
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        terminal.execute(input);
        return terminal.isTargetExecuted();
    }

    @Override
    public String getSuccessFeedback() {
        return terminal.getLastOutput();
    }

    @Override
    public String getFailureFeedback() {
        return terminal.getLastOutput();
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.TERMINAL;
    }

    public VirtualTerminal getTerminal() {
        return terminal;
    }
}
