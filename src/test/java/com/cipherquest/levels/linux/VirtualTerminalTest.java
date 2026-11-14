package com.cipherquest.levels.linux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualTerminalTest {

    private VirtualTerminal terminal;

    @BeforeEach
    void setUp() {
        terminal = new VirtualTerminal();
    }

    @Test
    void testInitialState() {
        assertEquals("/home/agent", terminal.getCurrentPath());
        assertFalse(terminal.isTargetExecuted());
    }

    @Test
    void testPwdAndWhoami() {
        assertEquals("/home/agent", terminal.execute("pwd"));
        assertEquals("agent", terminal.execute("whoami"));
    }

    @Test
    void testCdNavigation() {
        terminal.execute("cd documents");
        assertEquals("/home/agent/documents", terminal.getCurrentPath());

        terminal.execute("cd ..");
        assertEquals("/home/agent", terminal.getCurrentPath());

        terminal.execute("cd /opt/security");
        assertEquals("/opt/security", terminal.getCurrentPath());

        terminal.execute("cd -");
        assertEquals("/home/agent", terminal.getCurrentPath());

        terminal.execute("cd ~");
        assertEquals("/home/agent", terminal.getCurrentPath());
    }

    @Test
    void testCatFile() {
        String content = terminal.execute("cat readme.txt");
        assertTrue(content.contains("SECURITY MEMO"));

        String err = terminal.execute("cat nonexistent.txt");
        assertTrue(err.contains("No such file or directory"));
    }

    @Test
    void testFindCommand() {
        String output = terminal.execute("find / -name breach.bin");
        assertTrue(output.contains("/opt/security/breach.bin"));
    }

    @Test
    void testExecuteTargetBinary() {
        terminal.execute("cd /opt/security");
        String output = terminal.execute("./breach.bin");
        assertTrue(terminal.isTargetExecuted());
        assertTrue(output.contains("ROOT ACCESS ACQUIRED"));
    }

    @Test
    void testExecuteDirectPath() {
        String output = terminal.execute("/opt/security/breach.bin");
        assertTrue(terminal.isTargetExecuted());
        assertTrue(output.contains("ROOT ACCESS ACQUIRED"));
    }

    @Test
    void testCommandNotFound() {
        String output = terminal.execute("random_cmd_123");
        assertEquals("bash: random_cmd_123: command not found", output);
    }
}

