package com.cipherquest.levels.linux;

import com.cipherquest.levels.linux.commands.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class VirtualTerminal implements AutoCloseable {

    private TerminalContext context;
    private final Map<String, Command> commands = new HashMap<>();
    private String lastOutput = "";

    public VirtualTerminal() {
        commands.put("ls", new LsCommand());
        commands.put("cat", new CatCommand());

        commands.put("pwd", (args, ctx) ->
            ctx.getCurrentPath().toString()
        );

        commands.put("cd", (args, ctx) -> {
            String target = args.isEmpty() ? "~" : args.get(0);

            if (target.equals("-")) {
                return ctx.goBack().toString();
            }

            Path path = ctx.resolve(target);

            if (!Files.exists(path)) {
                return "cd: No such file or directory";
            }

            if (!Files.isDirectory(path)) {
                return "cd: Not a directory";
            }

            ctx.changeDirectory(path);
            return "";
        });

        reset();
    }

    public void reset() {
        try {
            if (context != null) {
                context.close();
            }
            context = new TerminalContext();
            lastOutput = "";
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to initialize terminal", e
            );
        }
    }

    public String execute(String input) {
        if (input == null || input.isBlank()) {
            lastOutput = "";
            return "";
        }

        String[] tokens = input.trim().split("\\s+");
        String name = tokens[0];
        List<String> args = Arrays.asList(tokens)
            .subList(1, tokens.length);

        try {
            Command command = commands.get(name);

            if (command != null) {
                lastOutput = command.execute(args, context);
            } else if (name.startsWith("./") ||
                       name.startsWith("/") ||
                       name.startsWith("../")) {
                lastOutput = executeFile(name);
            } else {
                lastOutput = "bash: " + name + ": command not found";
            }

        } catch (IOException e) {
            lastOutput = "Error: " + e.getMessage();
        }

        return lastOutput;
    }

    private String executeFile(String input) {
        Path path = context.resolve(input);

        if (context.isExecutable(path)) {
            context.markTargetExecuted();
            return "[+] ROOT ACCESS ACQUIRED!\n"
                 + "[+] Checkpoint 2 cleared.";
        }

        if (Files.exists(path)) {
            return "bash: Permission denied";
        }

        return "bash: No such file or directory";
    }

    public boolean isTargetExecuted() {
        return context.isTargetExecuted();
    }

    public String getCurrentPath() {
        return context.getCurrentPath().toString();
    }

    public String getLastOutput() {
        return lastOutput;
    }

    @Override
    public void close() throws IOException {
        context.close();
    }
}