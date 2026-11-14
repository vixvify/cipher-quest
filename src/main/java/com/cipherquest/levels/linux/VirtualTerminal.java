package com.cipherquest.levels.linux;

import com.cipherquest.levels.linux.commands.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VirtualTerminal implements AutoCloseable {
    private TerminalContext context;
    private final Map<String, Command> commands = new HashMap<>();
    private final ExecuteCommand executeCommand = new ExecuteCommand();
    private String lastOutput = "";

    public VirtualTerminal() {
        commands.put("pwd", new PwdCommand());
        commands.put("whoami", new WhoamiCommand());
        commands.put("uname", new UnameCommand());
        commands.put("clear", new ClearCommand());
        commands.put("help", new HelpCommand());
        commands.put("echo", new EchoCommand());
        commands.put("cd", new CdCommand());
        commands.put("ls", new LsCommand());
        commands.put("cat", new CatCommand());
        commands.put("find", new FindCommand());
        reset();
    }

    public void reset() {
        try {
            if (context != null) context.close();
            context = new TerminalContext();
            lastOutput = "";
        } catch (IOException e) {
            throw new IllegalStateException("Could not reset virtual terminal", e);
        }
    }

    public String execute(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            lastOutput = "";
            return "";
        }
        try {
            List<String> words = CommandLineParser.parse(rawCommand);
            if (words.isEmpty()) return lastOutput = "";
            String name = words.get(0);
            Command command = commands.get(name);
            if (command != null) {
                lastOutput = command.execute(words.subList(1, words.size()), context);
            } else if (name.startsWith("./") || name.startsWith("../") || name.startsWith("/")
                    || context.isExecutable(context.resolve(name))) {
                lastOutput = executeCommand.execute(List.of(name), context);
            } else {
                lastOutput = "bash: " + name + ": command not found";
            }
        } catch (IllegalArgumentException e) {
            lastOutput = "bash: " + e.getMessage();
        } catch (IOException e) {
            lastOutput = "terminal: " + e.getMessage();
        }
        return lastOutput;
    }

    public boolean isTargetExecuted() { return context.isTargetExecuted(); }
    public String getCurrentPath() { return context.currentPath().toString(); }
    public String getLastOutput() { return lastOutput; }

    @Override public void close() throws IOException {
        if (context != null) context.close();
    }
}
