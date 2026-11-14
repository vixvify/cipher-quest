package com.cipherquest.levels.linux;

import java.util.*;

public class VirtualTerminal {

    private String currentPath;
    private String previousPath;
    private boolean targetExecuted;
    private String lastOutput;

    private final Set<String> directories = new TreeSet<>();
    private final Map<String, String> files = new TreeMap<>();
    private final Set<String> executables = new HashSet<>();

    public VirtualTerminal() {
        reset();
    }

    public void reset() {
        this.currentPath = "/home/agent";
        this.previousPath = "/home/agent";
        this.targetExecuted = false;
        this.lastOutput = "";

        directories.clear();
        files.clear();
        executables.clear();

        directories.add("/");
        directories.add("/bin");
        directories.add("/etc");
        directories.add("/home");
        directories.add("/home/agent");
        directories.add("/home/agent/documents");
        directories.add("/opt");
        directories.add("/opt/security");
        directories.add("/var");
        directories.add("/var/log");

        files.put("/home/agent/readme.txt",
                "SECURITY MEMO:\n" +
                "The system security breach tool has been moved to /opt/security/\n" +
                "Find the binary file and execute it to override access controls.");

        files.put("/home/agent/notes.txt",
                "Command cheat sheet:\n" +
                "  ls -la               : list all files including hidden ones\n" +
                "  cd <path>            : change directory\n" +
                "  cat <file>           : read file contents\n" +
                "  find / -name <file>  : search entire system\n" +
                "  ./<program>          : run executable program");

        files.put("/home/agent/.bash_history",
                "cd /opt\nls -la\ncat /var/log/syslog");

        files.put("/home/agent/documents/mission.txt",
                "TARGET: breach.bin\nLOCATION: /opt/security/breach.bin\nClearance required: execute");

        files.put("/etc/hostname", "cipherquest-terminal");
        files.put("/etc/os-release", "NAME=CipherQuest Linux\nVERSION=2.5.0\nID=cipherquest");
        files.put("/var/log/syslog",
                "systemd[1]: started security monitoring\n" +
                "kernel: isolated executable placed at /opt/security/breach.bin\n" +
                "audit: agent terminal session active");

        files.put("/opt/security/breach.bin", "ELF_BINARY_DATA_BREACH_TOOL");
        executables.add("/opt/security/breach.bin");
    }

    public String execute(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            this.lastOutput = "";
            return "";
        }

        String input = rawCommand.trim();
        String[] tokens = input.split("\\s+");
        String cmd = tokens[0];

        String output;
        switch (cmd) {
            case "pwd" -> output = currentPath;
            case "whoami" -> output = "agent";
            case "uname" -> {
                if (tokens.length > 1 && tokens[1].equals("-a")) {
                    output = "Linux cipherquest-terminal 5.15.0-x86_64 GNU/Linux";
                } else {
                    output = "Linux";
                }
            }
            case "clear" -> output = "__CLEAR__";
            case "help" -> output =
                    "Available commands:\n" +
                    "  ls [options] [path]  - List directory contents (-a, -l, -la)\n" +
                    "  cd <path>            - Change current directory\n" +
                    "  pwd                  - Print working directory\n" +
                    "  cat <file>           - Display file content\n" +
                    "  find <path> [-name]  - Search files\n" +
                    "  whoami               - Print current user\n" +
                    "  uname [-a]           - Print system info\n" +
                    "  echo <text>          - Print text\n" +
                    "  clear                - Clear terminal screen\n" +
                    "  ./<binary>           - Execute program";
            case "echo" -> {
                output = input.length() > 5 ? input.substring(5).trim() : "";
            }
            case "cd" -> {
                String target = tokens.length > 1 ? tokens[1] : "/home/agent";
                output = handleCd(target);
            }
            case "ls" -> output = handleLs(tokens);
            case "cat" -> {
                if (tokens.length < 2) {
                    output = "cat: missing file operand";
                } else {
                    output = handleCat(tokens[1]);
                }
            }
            case "find" -> output = handleFind(tokens);
            default -> {
                if (cmd.startsWith("./") || cmd.startsWith("/") || executables.contains(resolvePath(cmd))) {
                    output = handleExecution(cmd);
                } else {
                    output = "bash: " + cmd + ": command not found";
                }
            }
        }

        this.lastOutput = output;
        return output;
    }

    private String handleCd(String target) {
        if (target.equals("~") || target.equals("")) {
            this.previousPath = currentPath;
            this.currentPath = "/home/agent";
            return "";
        }
        if (target.equals("-")) {
            String temp = currentPath;
            this.currentPath = previousPath;
            this.previousPath = temp;
            return currentPath;
        }

        String resolved = resolvePath(target);
        if (directories.contains(resolved)) {
            this.previousPath = currentPath;
            this.currentPath = resolved;
            return "";
        }

        if (files.containsKey(resolved)) {
            return "bash: cd: " + target + ": Not a directory";
        }
        return "bash: cd: " + target + ": No such file or directory";
    }

    private String handleLs(String[] tokens) {
        boolean showAll = false;
        boolean showLong = false;
        String targetPath = currentPath;

        for (int i = 1; i < tokens.length; i++) {
            String arg = tokens[i];
            if (arg.startsWith("-")) {
                if (arg.contains("a")) showAll = true;
                if (arg.contains("l")) showLong = true;
            } else {
                targetPath = resolvePath(arg);
            }
        }

        if (!directories.contains(targetPath)) {
            if (files.containsKey(targetPath)) {
                return getFileName(targetPath);
            }
            return "ls: cannot access '" + targetPath + "': No such file or directory";
        }

        List<String> items = new ArrayList<>();
        if (showAll) {
            items.add(".");
            items.add("..");
        }

        String dirPrefix = targetPath.equals("/") ? "/" : targetPath + "/";

        for (String dir : directories) {
            if (dir.startsWith(dirPrefix) && !dir.equals(targetPath)) {
                String sub = dir.substring(dirPrefix.length());
                if (!sub.contains("/")) {
                    if (showAll || !sub.startsWith(".")) {
                        items.add(sub + "/");
                    }
                }
            }
        }

        for (String file : files.keySet()) {
            if (file.startsWith(dirPrefix)) {
                String sub = file.substring(dirPrefix.length());
                if (!sub.contains("/")) {
                    if (showAll || !sub.startsWith(".")) {
                        items.add(sub);
                    }
                }
            }
        }

        Collections.sort(items);

        if (showLong) {
            StringBuilder sb = new StringBuilder();
            sb.append("total ").append(items.size()).append("\n");
            for (String item : items) {
                boolean isDir = item.endsWith("/");
                String perms = isDir ? "drwxr-xr-x" : "-rw-r--r--";
                if (executables.contains(dirPrefix + item.replace("/", ""))) {
                    perms = "-rwxr-xr-x";
                }
                sb.append(String.format("%s  1 agent agent  %4d  Nov 14 12:00  %s\n",
                        perms, isDir ? 4096 : 512, item));
            }
            return sb.toString().trim();
        } else {
            return String.join("  ", items);
        }
    }

    private String handleCat(String target) {
        String resolved = resolvePath(target);
        if (directories.contains(resolved)) {
            return "cat: " + target + ": Is a directory";
        }
        if (files.containsKey(resolved)) {
            return files.get(resolved);
        }
        return "cat: " + target + ": No such file or directory";
    }

    private String handleFind(String[] tokens) {
        String startPath = currentPath;
        String pattern = null;

        for (int i = 1; i < tokens.length; i++) {
            if (tokens[i].equals("-name") && i + 1 < tokens.length) {
                pattern = tokens[i + 1];
                i++;
            } else if (!tokens[i].startsWith("-")) {
                startPath = resolvePath(tokens[i]);
            }
        }

        String prefix = startPath.equals("/") ? "/" : startPath + "/";
        List<String> matched = new ArrayList<>();

        if (startPath.equals("/") || directories.contains(startPath)) {
            matched.add(startPath);
        }

        for (String dir : directories) {
            if (dir.startsWith(prefix)) {
                String name = getFileName(dir);
                if (pattern == null || matchPattern(name, pattern)) {
                    matched.add(dir);
                }
            }
        }

        for (String file : files.keySet()) {
            if (file.startsWith(prefix) || file.equals(startPath)) {
                String name = getFileName(file);
                if (pattern == null || matchPattern(name, pattern)) {
                    matched.add(file);
                }
            }
        }

        Collections.sort(matched);
        return String.join("\n", matched);
    }

    private boolean matchPattern(String name, String pattern) {
        if (pattern.equals("*")) return true;
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            return name.contains(pattern.substring(1, pattern.length() - 1));
        }
        if (pattern.startsWith("*")) {
            return name.endsWith(pattern.substring(1));
        }
        if (pattern.endsWith("*")) {
            return name.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return name.equalsIgnoreCase(pattern);
    }

    private String handleExecution(String command) {
        String resolved = resolvePath(command.replaceFirst("^\\./", ""));
        if (!resolved.startsWith("/")) {
            resolved = resolvePath(command);
        }

        if (executables.contains(resolved)) {
            this.targetExecuted = true;
            return "[+] Launching " + resolved + "...\n" +
                   "[+] Injecting memory payload into kernel space...\n" +
                   "[+] Authentication bypass: [##########] 100%\n" +
                   "[+] ROOT ACCESS ACQUIRED! Checkpoint 2 cleared.";
        }

        if (files.containsKey(resolved)) {
            return "bash: " + command + ": Permission denied";
        }

        return "bash: " + command + ": No such file or directory";
    }

    private String resolvePath(String path) {
        if (path == null || path.isEmpty()) {
            return currentPath;
        }

        if (path.startsWith("~")) {
            path = "/home/agent" + path.substring(1);
        }

        List<String> parts = new ArrayList<>();
        if (!path.startsWith("/")) {
            for (String p : currentPath.split("/")) {
                if (!p.isEmpty()) parts.add(p);
            }
        }

        for (String segment : path.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..")) {
                if (!parts.isEmpty()) {
                    parts.remove(parts.size() - 1);
                }
            } else {
                parts.add(segment);
            }
        }

        return "/" + String.join("/", parts);
    }

    private String getFileName(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    public boolean isTargetExecuted() {
        return targetExecuted;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public String getLastOutput() {
        return lastOutput;
    }
}

