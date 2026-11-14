package com.cipherquest.levels.linux;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Holds one level's isolated, in-memory file system and terminal state. */
public final class TerminalContext implements AutoCloseable {
    public static final String HOME = "/home/agent";
    public static final String TARGET = "/opt/security/breach.bin";

    private final FileSystem fileSystem;
    private final Set<Path> executables = new HashSet<>();
    private Path currentPath;
    private Path previousPath;
    private boolean targetExecuted;

    public TerminalContext() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        currentPath = fileSystem.getPath(HOME);
        previousPath = currentPath;
        seed();
    }

    private void seed() throws IOException {
        for (String directory : new String[] {
                "/bin", "/etc", "/home/agent/documents", "/opt/security", "/var/log"}) {
            Files.createDirectories(fileSystem.getPath(directory));
        }
        write("/home/agent/readme.txt",
                "SECURITY MEMO:\n" +
                "The system security breach tool has been moved to /opt/security/\n" +
                "Find the binary file and execute it to override access controls.");
        write("/home/agent/notes.txt",
                "Command cheat sheet:\n" +
                "  ls -la               : list all files including hidden ones\n" +
                "  cd <path>            : change directory\n" +
                "  cat <file>           : read file contents\n" +
                "  find / -name <file>  : search entire system\n" +
                "  ./<program>          : run executable program");
        write("/home/agent/.bash_history", "cd /opt\nls -la\ncat /var/log/syslog");
        write("/home/agent/documents/mission.txt",
                "TARGET: breach.bin\nLOCATION: /opt/security/breach.bin\nClearance required: execute");
        write("/etc/hostname", "cipherquest-terminal");
        write("/etc/os-release", "NAME=CipherQuest Linux\nVERSION=2.5.0\nID=cipherquest");
        write("/var/log/syslog",
                "systemd[1]: started security monitoring\n" +
                "kernel: isolated executable placed at /opt/security/breach.bin\n" +
                "audit: agent terminal session active");
        Path binary = fileSystem.getPath(TARGET);
        write(TARGET, "ELF_BINARY_DATA_BREACH_TOOL");
        executables.add(binary);
    }

    private void write(String path, String content) throws IOException {
        Files.writeString(fileSystem.getPath(path), content);
    }

    public Path resolve(String input) {
        if (input == null || input.isEmpty()) return currentPath;
        if (input.equals("~") || input.startsWith("~/")) {
            input = HOME + input.substring(1);
        }
        Path path = fileSystem.getPath(input);
        return (path.isAbsolute() ? path : currentPath.resolve(path)).normalize();
    }

    public Path currentPath() { return currentPath; }
    public FileSystem fileSystem() { return fileSystem; }
    public boolean isExecutable(Path path) { return executables.contains(path.normalize()); }
    public boolean isTargetExecuted() { return targetExecuted; }
    public void markTargetExecuted() { targetExecuted = true; }

    public void changeDirectory(Path path) {
        previousPath = currentPath;
        currentPath = path.normalize();
    }

    public Path switchToPreviousDirectory() {
        Path oldCurrent = currentPath;
        currentPath = previousPath;
        previousPath = oldCurrent;
        return currentPath;
    }

    @Override public void close() throws IOException { fileSystem.close(); }
}
