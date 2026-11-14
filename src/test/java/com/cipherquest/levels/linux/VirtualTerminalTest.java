package com.cipherquest.levels.linux;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VirtualTerminalTest {
    @Test void basicCommands() throws Exception {
        try (VirtualTerminal t = new VirtualTerminal()) {
            assertEquals("/home/agent", t.execute("pwd"));
            assertEquals("agent", t.execute("whoami"));
            assertEquals("Linux", t.execute("uname"));
            assertTrue(t.execute("uname -a").contains("GNU/Linux"));
            assertEquals("hello world", t.execute("echo \"hello world\""));
            assertEquals("__CLEAR__", t.execute("clear"));
            assertTrue(t.execute("help").contains("find"));
            assertEquals("bash: xyz: command not found", t.execute("xyz"));
        }
    }

    @Test void directoriesAndFiles() throws Exception {
        try (VirtualTerminal t = new VirtualTerminal()) {
            assertTrue(t.execute("ls").contains("readme.txt"));
            assertFalse(t.execute("ls").contains(".bash_history"));
            assertTrue(t.execute("ls -la").contains(".bash_history"));
            assertTrue(t.execute("ls -la /opt/security").contains("-rwxr-xr-x"));
            assertTrue(t.execute("cat readme.txt").contains("SECURITY MEMO"));
            assertTrue(t.execute("cat /home/agent/documents/mission.txt").contains("breach.bin"));
            assertTrue(t.execute("cat missing").contains("No such file"));
            assertTrue(t.execute("cat documents").contains("Is a directory"));
            assertEquals("", t.execute("cd /opt/security"));
            assertEquals("/opt/security", t.getCurrentPath());
            assertEquals("/home/agent", t.execute("cd -"));
            assertEquals("", t.execute("cd .."));
            assertEquals("/home", t.execute("pwd"));
            assertEquals("", t.execute("cd ~"));
            assertEquals("/home/agent", t.execute("pwd"));
            assertTrue(t.execute("cd missing").contains("No such file"));
        }
    }

    @Test void findAndExecuteAndReset() throws Exception {
        try (VirtualTerminal t = new VirtualTerminal()) {
            assertEquals("/opt/security/breach.bin", t.execute("find / -name breach.bin"));
            assertTrue(t.execute("find / -name '*.txt'").contains("readme.txt"));
            assertTrue(t.execute("./missing.bin").contains("No such file"));
            assertTrue(t.execute("./readme.txt").contains("Permission denied"));
            assertFalse(t.isTargetExecuted());
            assertEquals("", t.execute("cd /opt/security"));
            assertTrue(t.execute("./breach.bin").contains("Checkpoint 2 cleared"));
            assertTrue(t.isTargetExecuted());
            assertEquals(t.execute("pwd"), t.getLastOutput());
            t.reset();
            assertFalse(t.isTargetExecuted());
            assertEquals("/home/agent", t.getCurrentPath());
            assertEquals("", t.getLastOutput());
        }
    }

    @Test void noHostExecutionAndParsingErrors() throws Exception {
        try (VirtualTerminal t = new VirtualTerminal()) {
            assertTrue(t.execute("rm -rf /").contains("command not found"));
            assertTrue(t.execute("echo 'unfinished").contains("unclosed quote"));
            assertTrue(t.execute("find / -name").contains("missing argument"));
            assertEquals("", t.execute("  "));
            assertEquals("", t.execute(null));
        }
    }
}
