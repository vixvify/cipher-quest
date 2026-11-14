package com.cipherquest.levels.linux;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TerminalContext implements AutoCloseable {
    private final FileSystem fs =
        Jimfs.newFileSystem(Configuration.unix());

    private Path currentPath = fs.getPath("/home/agent");
    private Path previousPath = currentPath;

    private final Set<Path> executables = new HashSet<>();
    private boolean targetExecuted = false;

    public TerminalContext() throws IOException {
        Files.createDirectories(fs.getPath("/home/agent"));
        Files.createDirectories(fs.getPath("/opt/security"));
        Files.createDirectories(fs.getPath("/var/log"));

        Files.writeString(
            fs.getPath("/home/agent/readme.txt"),
            "Find and execute breach.bin"
        );

        Files.writeString(
            fs.getPath("/opt/security/breach.bin"),
            "SIMULATED_BINARY"
        );

        executables.add(fs.getPath("/opt/security/breach.bin"));
    }

    public Path resolve(String input) {
        if (input.startsWith("~")) {
            input = "/home/agent" + input.substring(1);
        }

        Path path = fs.getPath(input);

        return (path.isAbsolute()
                ? path
                : currentPath.resolve(path))
                .normalize();
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public void changeDirectory(Path path) {
        previousPath = currentPath;
        currentPath = path;
    }

    public Path goBack() {
        Path old = currentPath;
        currentPath = previousPath;
        previousPath = old;
        return currentPath;
    }

    public FileSystem getFileSystem() {
        return fs;
    }

    public boolean isExecutable(Path path) {
        return executables.contains(path);
    }

    public void markTargetExecuted() {
        targetExecuted = true;
    }

    public boolean isTargetExecuted() {
        return targetExecuted;
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}