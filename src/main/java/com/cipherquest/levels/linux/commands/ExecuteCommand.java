package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Simulates execution; never launches a process on the player's machine. */
public final class ExecuteCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) {
        String command = args.get(0);
        Path path = ctx.resolve(command);
        if (ctx.isExecutable(path)) {
            if (path.equals(ctx.resolve(TerminalContext.TARGET))) {
                ctx.markTargetExecuted();
            }
            return "[+] Launching " + path + "...\n" +
                   "[+] Injecting memory payload into kernel space...\n" +
                   "[+] Authentication bypass: [##########] 100%\n" +
                   "[+] ROOT ACCESS ACQUIRED! Checkpoint 2 cleared.";
        }
        if (Files.exists(path)) return "bash: " + command + ": Permission denied";
        return "bash: " + command + ": No such file or directory";
    }
}
